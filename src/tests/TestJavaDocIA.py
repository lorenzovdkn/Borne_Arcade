#!/usr/bin/env python3

import sys
import time
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent / "IA" / "API"))

from ollama_wrapper import OllamaWrapper


class TestJavaDocIA:
    def __init__(self):
        self.client = OllamaWrapper()
        self.project_root = Path(__file__).resolve().parents[2]
        self.results = []

    def _record(self, name, status, message=""):
        self.results.append((name, status, message))
        suffix = f" - {message}" if message else ""
        print(f"[{status}] {name}{suffix}")

    def test_detect(self):
        undocumented = self.client.detect_undocumented_functions(project_root=self.project_root)
        assert isinstance(undocumented, list)
        if undocumented:
            sample = undocumented[0]
            for key in ["file", "line", "class_name", "function_name", "signature", "code"]:
                assert key in sample
        self._record("detect", "PASS", f"{len(undocumented)} fonction(s) trouvée(s)")
        return undocumented

    def test_extracts(self, undocumented):
        if not undocumented:
            self._record("extract", "SKIP", "aucune fonction trouvée")
            return

        for func in undocumented[:3]:
            assert func["signature"].strip()
            assert "{" in func["code"] and "}" in func["code"]
        self._record("extract", "PASS", "signatures/corps valides")

    def test_server(self):
        running = self.client.is_server_running()
        if not running:
            self._record("server", "SKIP", "ollama indisponible")
            return False

        version = self.client.get_version()
        assert isinstance(version, str) and version
        self._record("server", "PASS", f"version {version}")
        return True

    def _select_model(self):
        try:
            models = self.client.list_models()
        except Exception:
            return None

        names = [m.name for m in models]
        for preferred in ["qwen3:8b", "qwen2:latest", "gemma2:latest", "mistral:latest"]:
            if preferred in names:
                return preferred
        for name in names:
            if "embedding" not in name.lower():
                return name
        return None

    def _generate_javadoc_with_retry(self, model, func, retries=2):
        last_error = None
        for attempt in range(retries + 1):
            try:
                return self.client.generate_javadoc_for_function(
                    model=model,
                    function_code=func["code"],
                    class_name=func["class_name"],
                    function_name=func["function_name"],
                )
            except Exception as e:
                last_error = e
                if attempt < retries:
                    time.sleep(0.6)
        raise last_error

    def test_generate(self, undocumented):
        if not undocumented:
            self._record("generate", "SKIP", "aucune fonction trouvée")
            return
        if not self.client.is_server_running():
            self._record("generate", "SKIP", "ollama indisponible")
            return

        model = self._select_model()
        if not model:
            self._record("generate", "SKIP", "pas de modèle texte")
            return

        func = undocumented[0]
        javadoc = self._generate_javadoc_with_retry(model, func)
        assert javadoc.startswith("/**")
        assert javadoc.rstrip().endswith("*/")
        self._record("generate", "PASS", f"modèle {model}")

    def test_apply_on_test_file(self):
        target_file = "src/tests/java/TestJavaDocGeneration.java"
        target_path = self.project_root / target_file

        if not target_path.exists():
            self._record("apply_test_file", "SKIP", "fichier cible introuvable")
            return

        if not self.client.is_server_running():
            self._record("apply_test_file", "SKIP", "ollama indisponible")
            return

        model = self._select_model()
        if not model:
            self._record("apply_test_file", "SKIP", "pas de modèle texte")
            return

        before = self.client.detect_undocumented_functions(project_root=self.project_root)
        before_target = [f for f in before if f["file"] == target_file]

        if not before_target:
            self._record("apply_test_file", "SKIP", "déjà documenté")
            return

        lines = target_path.read_text(encoding="utf-8", errors="replace").splitlines()
        added = 0

        for func in sorted(before_target, key=lambda item: item["line"], reverse=True):
            try:
                javadoc = self._generate_javadoc_with_retry(model, func)
            except Exception:
                continue

            index = func["line"] - 1
            indent = len(lines[index]) - len(lines[index].lstrip())
            prefix = " " * indent
            block = [prefix + ln if ln.strip() else "" for ln in javadoc.splitlines()]
            lines[index:index] = block + [""]
            added += 1

        if added == 0:
            self._record("apply_test_file", "SKIP", "génération indisponible")
            return

        target_path.write_text("\n".join(lines) + "\n", encoding="utf-8")

        after = self.client.detect_undocumented_functions(project_root=self.project_root)
        after_target = [f for f in after if f["file"] == target_file]

        assert len(after_target) < len(before_target), "aucun commentaire ajouté"
        self._record(
            "apply_test_file",
            "PASS",
            f"{len(before_target) - len(after_target)} commentaire(s) ajouté(s)",
        )

    def run(self):
        undocumented = []

        try:
            undocumented = self.test_detect()
        except Exception as e:
            self._record("detect", "FAIL", str(e))

        try:
            self.test_extracts(undocumented)
        except Exception as e:
            self._record("extract", "FAIL", str(e))

        try:
            self.test_server()
        except Exception as e:
            self._record("server", "FAIL", str(e))

        try:
            self.test_generate(undocumented)
        except Exception as e:
            self._record("generate", "FAIL", str(e))

        try:
            self.test_apply_on_test_file()
        except Exception as e:
            self._record("apply_test_file", "FAIL", str(e))

        total = len(self.results)
        passed = sum(1 for _, status, _ in self.results if status == "PASS")
        failed = sum(1 for _, status, _ in self.results if status == "FAIL")
        skipped = sum(1 for _, status, _ in self.results if status == "SKIP")

        print("\nRésumé:")
        print(f"- total: {total}")
        print(f"- pass: {passed}")
        print(f"- fail: {failed}")
        print(f"- skip: {skipped}")

        if failed > 0:
            raise SystemExit(1)
        raise SystemExit(0)


def main():
    TestJavaDocIA().run()


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("Interrompu.")
        sys.exit(1)
    except Exception as e:
        print(f"Erreur: {e}")
        sys.exit(1)
