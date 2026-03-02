from __future__ import annotations

import base64
import json
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Dict, List, Mapping, Optional, Sequence, Union


class OllamaError(RuntimeError):
    pass


class OllamaConnectionError(OllamaError):
    pass


class OllamaResponseError(OllamaError):
    pass


@dataclass(frozen=True, slots=True)
class OllamaModelInfo:
    name: str


@dataclass(frozen=True, slots=True)
class OllamaGenerateResult:
    response: str
    model: Optional[str] = None
    done: Optional[bool] = None


class OllamaWrapper:
    def __init__(self, base_url: str = "http://10.22.28.190:11434", timeout_s: float = 60.0) -> None:
        self._base_url = base_url.rstrip("/")
        self._timeout_s = timeout_s

    def _http_request_json(
        self,
        method: str,
        path: str,
        *,
        body: Optional[Mapping[str, Any]] = None,
    ) -> Dict[str, Any]:
        import urllib.error
        import urllib.request
        from urllib.parse import urljoin

        url = urljoin(self._base_url + "/", path.lstrip("/"))
        data = json.dumps(body).encode("utf-8") if body is not None else None

        request = urllib.request.Request(
            url=url,
            data=data,
            headers={"Accept": "application/json", "Content-Type": "application/json"},
            method=method.upper(),
        )

        try:
            with urllib.request.urlopen(request, timeout=self._timeout_s) as response:
                text = response.read().decode("utf-8", errors="replace")
        except urllib.error.URLError as e:
            raise OllamaConnectionError(f"Impossible de joindre Ollama ({url}): {e}") from e
        except Exception as e:
            raise OllamaConnectionError(f"Erreur réseau ({url}): {e}") from e

        try:
            payload = json.loads(text)
        except json.JSONDecodeError as e:
            raise OllamaResponseError(f"Réponse non JSON ({url}): {text[:200]!r}") from e

        if not isinstance(payload, dict):
            raise OllamaResponseError(f"Réponse inattendue ({url}): {payload!r}")
        return payload

    def is_server_running(self) -> bool:
        try:
            self.get_version()
            return True
        except OllamaError:
            return False

    def get_version(self) -> str:
        payload = self._http_request_json("GET", "/api/version")
        version = payload.get("version")
        if not isinstance(version, str):
            raise OllamaResponseError(f"Réponse /api/version invalide: {payload!r}")
        return version

    def list_models(self) -> List[OllamaModelInfo]:
        payload = self._http_request_json("GET", "/api/tags")
        raw_models = payload.get("models")
        if not isinstance(raw_models, list):
            return []

        models: List[OllamaModelInfo] = []
        for item in raw_models:
            if isinstance(item, dict) and isinstance(item.get("name"), str):
                models.append(OllamaModelInfo(name=item["name"]))
        return models

    def generate_text(
        self,
        *,
        model: str,
        prompt: str,
        system: Optional[str] = None,
        options: Optional[Mapping[str, Any]] = None,
    ) -> OllamaGenerateResult:
        body: Dict[str, Any] = {
            "model": model,
            "prompt": prompt,
            "stream": False,
        }
        if system:
            body["system"] = system
        if options:
            body["options"] = dict(options)

        payload = self._http_request_json("POST", "/api/generate", body=body)
        response = payload.get("response")
        if not isinstance(response, str):
            raise OllamaResponseError(f"Réponse /api/generate invalide: {payload!r}")

        return OllamaGenerateResult(
            response=response,
            model=payload.get("model") if isinstance(payload.get("model"), str) else None,
            done=payload.get("done") if isinstance(payload.get("done"), bool) else None,
        )

    def generate_with_image(
        self,
        *,
        model: str,
        prompt: str,
        image: Union[str, Path, bytes],
        system: Optional[str] = None,
        options: Optional[Mapping[str, Any]] = None,
    ) -> OllamaGenerateResult:
        if isinstance(image, (str, Path)):
            image_bytes = Path(image).read_bytes()
        elif isinstance(image, (bytes, bytearray)):
            image_bytes = bytes(image)
        else:
            raise TypeError("image doit être un chemin ou des bytes")

        body: Dict[str, Any] = {
            "model": model,
            "prompt": prompt,
            "images": [base64.b64encode(image_bytes).decode("ascii")],
            "stream": False,
        }
        if system:
            body["system"] = system
        if options:
            body["options"] = dict(options)

        payload = self._http_request_json("POST", "/api/generate", body=body)
        response = payload.get("response")
        if not isinstance(response, str):
            raise OllamaResponseError(f"Réponse /api/generate invalide: {payload!r}")
        return OllamaGenerateResult(response=response)

    def detect_undocumented_functions(self, project_root: Union[str, Path] = ".") -> List[Dict[str, Any]]:
        root = Path(project_root)
        scan_dirs = [
            root / "src" / "main" / "java",
            root / "src" / "IA" / "API",
            root / "src" / "tests" / "java",
        ]

        undocumented: List[Dict[str, Any]] = []
        for scan_dir in scan_dirs:
            if not scan_dir.exists():
                continue
            for java_file in scan_dir.rglob("*.java"):
                self._analyze_java_file(java_file, root, undocumented)
        return undocumented

    def _analyze_java_file(self, java_file: Path, project_root: Path, out: List[Dict[str, Any]]) -> None:
        try:
            lines = java_file.read_text(encoding="utf-8", errors="replace").splitlines()
        except Exception:
            return

        content = "\n".join(lines)
        class_match = re.search(r"class\s+(\w+)", content)
        class_name = class_match.group(1) if class_match else "Unknown"

        func_pattern = re.compile(
            r"(?:public|private|protected|static|final|native|synchronized|abstract|\s)+"
            r"[\w<>\[\], ?]+\s+(\w+)\s*\([^)]*\)\s*(?:throws\s+[^{]+)?\{"
        )

        for index, line in enumerate(lines):
            match = func_pattern.search(line)
            if not match:
                continue
            if self._has_javadoc(lines, index):
                continue

            out.append(
                {
                    "file": str(java_file.relative_to(project_root)),
                    "line": index + 1,
                    "class_name": class_name,
                    "function_name": match.group(1),
                    "signature": self._extract_function_signature(lines, index),
                    "code": self._extract_function_body(lines, index),
                }
            )

    def _has_javadoc(self, lines: Sequence[str], func_line: int) -> bool:
        i = func_line - 1
        while i >= 0 and lines[i].strip() == "":
            i -= 1

        if i < 0:
            return False

        if lines[i].strip().startswith("@"):
            while i >= 0 and lines[i].strip().startswith("@"):
                i -= 1

        if i < 0 or not lines[i].strip().endswith("*/"):
            return False

        while i >= 0:
            stripped = lines[i].strip()
            if stripped.startswith("/**"):
                return True
            if stripped.startswith("/*"):
                return False
            i -= 1
        return False

    def _extract_function_signature(self, lines: Sequence[str], start: int) -> str:
        parts: List[str] = []
        i = start
        while i < len(lines) and i < start + 10:
            parts.append(lines[i].strip())
            if "{" in lines[i]:
                break
            i += 1
        return " ".join(parts).strip()

    def _extract_function_body(self, lines: Sequence[str], start: int, max_lines: int = 80) -> str:
        body: List[str] = []
        brace_count = 0
        opened = False

        for i in range(start, min(start + max_lines, len(lines))):
            line = lines[i]
            body.append(line)
            brace_count += line.count("{")
            if "{" in line:
                opened = True
            brace_count -= line.count("}")
            if opened and brace_count <= 0:
                break

        return "\n".join(body)

    def generate_javadoc_for_function(
        self,
        *,
        model: str,
        function_code: str,
        class_name: str,
        function_name: str,
    ) -> str:
        prompt = (
            "Génère un commentaire Javadoc en français pour cette fonction Java.\n"
            "Retourne uniquement le bloc Javadoc (/** ... */).\n\n"
            f"Classe: {class_name}\n"
            f"Fonction: {function_name}\n\n"
            "Code:\n"
            "```java\n"
            f"{function_code}\n"
            "```"
        )

        result = self.generate_text(
            model=model,
            prompt=prompt,
            system="Tu écris des Javadocs courts, précis et corrects.",
        )

        javadoc = result.response.strip()
        if not javadoc.startswith("/**"):
            javadoc = "/**\n * " + javadoc.strip("/* ") + "\n */"
        if not javadoc.endswith("*/"):
            javadoc = javadoc.rstrip() + "\n */"
        return javadoc

    def _select_generation_model(self) -> Optional[str]:
        try:
            model_names = [m.name for m in self.list_models()]
        except OllamaError:
            return None

        preferred = ["qwen3:8b", "qwen2:latest", "gemma2:latest", "mistral:latest"]
        for name in preferred:
            if name in model_names:
                return name

        for name in model_names:
            if "embedding" not in name.lower():
                return name
        return None

    def auto_document_java_files(
        self,
        *,
        project_root: Union[str, Path] = ".",
        model: Optional[str] = None,
        dry_run: bool = False,
    ) -> Dict[str, Any]:
        root = Path(project_root)
        stats: Dict[str, Any] = {
            "total_scanned": 0,
            "functions_found": 0,
            "functions_documented": 0,
            "functions_failed": 0,
            "files_modified": [],
            "errors": [],
        }

        model_name = model or self._select_generation_model()
        if not model_name:
            stats["errors"].append("Aucun modèle de génération disponible")
            return stats

        undocumented = self.detect_undocumented_functions(project_root=root)
        stats["functions_found"] = len(undocumented)
        if not undocumented:
            return stats

        by_file: Dict[str, List[Dict[str, Any]]] = {}
        for func in undocumented:
            by_file.setdefault(func["file"], []).append(func)

        for relative_file, functions in by_file.items():
            file_path = root / relative_file
            stats["total_scanned"] += 1

            if not file_path.exists():
                stats["errors"].append(f"Fichier non trouvé: {relative_file}")
                continue

            try:
                lines = file_path.read_text(encoding="utf-8", errors="replace").splitlines()
            except Exception as e:
                stats["errors"].append(f"Lecture impossible {relative_file}: {e}")
                continue

            modified = False
            for func in sorted(functions, key=lambda f: f["line"], reverse=True):
                try:
                    javadoc = self.generate_javadoc_for_function(
                        model=model_name,
                        function_code=func["code"],
                        class_name=func["class_name"],
                        function_name=func["function_name"],
                    )

                    stats["functions_documented"] += 1
                    if dry_run:
                        continue

                    line_index = func["line"] - 1
                    indent = len(lines[line_index]) - len(lines[line_index].lstrip())
                    prefix = " " * indent
                    block = [prefix + ln if ln.strip() else "" for ln in javadoc.splitlines()]
                    lines[line_index:line_index] = block + [""]
                    modified = True
                except Exception as e:
                    stats["functions_failed"] += 1
                    stats["errors"].append(f"{relative_file}:{func['line']} {e}")

            if modified:
                file_path.write_text("\n".join(lines) + "\n", encoding="utf-8")
                stats["files_modified"].append(relative_file)

        return stats
