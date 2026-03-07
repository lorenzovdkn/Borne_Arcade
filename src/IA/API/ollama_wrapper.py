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
    """
    Wrapper Python pour l'API Ollama.
    Permet de communiquer avec un serveur Ollama pour générer du texte,
    analyser des images et auto-documenter du code Java.
    """

    def __init__(self, base_url: str = "http://10.22.28.190:11434", timeout_s: float = 300.0) -> None:
        """
        Initialise le wrapper Ollama.

        :param base_url: URL de base du serveur Ollama (par défaut: serveur IUT)
        :param timeout_s: Timeout en secondes pour les requêtes HTTP
        """
        self._base_url = base_url.rstrip("/")
        self._timeout_s = timeout_s

    def _http_request_json(
        self,
        method: str,
        path: str,
        *,
        body: Optional[Mapping[str, Any]] = None,
    ) -> Dict[str, Any]:
        """
        Effectue une requête HTTP vers l'API Ollama et retourne la réponse JSON.

        :param method: Méthode HTTP (GET, POST, etc.)
        :param path: Chemin de l'endpoint API
        :param body: Corps de la requête (optionnel)
        :return: Dictionnaire contenant la réponse JSON
        :raises OllamaConnectionError: Si la connexion échoue
        :raises OllamaResponseError: Si la réponse n'est pas du JSON valide
        """
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
        """
        Vérifie si le serveur Ollama est accessible.

        :return: True si le serveur répond, False sinon
        """
        try:
            self.get_version()
            return True
        except OllamaError:
            return False

    def get_version(self) -> str:
        """
        Récupère la version du serveur Ollama.

        :return: Chaîne de version du serveur
        :raises OllamaResponseError: Si la réponse est invalide
        """
        payload = self._http_request_json("GET", "/api/version")
        version = payload.get("version")
        if not isinstance(version, str):
            raise OllamaResponseError(f"Réponse /api/version invalide: {payload!r}")
        return version

    def list_models(self) -> List[OllamaModelInfo]:
        """
        Liste tous les modèles disponibles sur le serveur Ollama.

        :return: Liste des modèles disponibles
        """
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
        """
        Génère du texte à partir d'un prompt en utilisant un modèle Ollama.

        :param model: Nom du modèle à utiliser (ex: 'qwen3:8b')
        :param prompt: Texte d'entrée pour la génération
        :param system: Prompt système optionnel pour configurer le comportement
        :param options: Options supplémentaires pour la génération
        :return: Résultat de la génération contenant la réponse
        :raises OllamaResponseError: Si la réponse est invalide
        """
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
        """
        Génère du texte à partir d'un prompt et d'une image (vision multimodale).

        :param model: Nom du modèle à utiliser (doit supporter la vision)
        :param prompt: Texte d'entrée pour la génération
        :param image: Image sous forme de chemin, Path ou bytes
        :param system: Prompt système optionnel
        :param options: Options supplémentaires pour la génération
        :return: Résultat de la génération contenant la réponse
        :raises TypeError: Si le format de l'image est invalide
        :raises OllamaResponseError: Si la réponse est invalide
        """
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
        """
        Détecte toutes les fonctions Java sans documentation Javadoc dans le projet.

        :param project_root: Racine du projet à scanner
        :return: Liste des fonctions non documentées avec leurs métadonnées
        """
        root = Path(project_root)
        # Scanner uniquement les fichiers du projet, pas les libs (MG2D, etc.)
        scan_dirs = [
            root / "src" / "main" / "java",
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
        """
        Analyse un fichier Java pour trouver les fonctions sans Javadoc.

        :param java_file: Chemin du fichier Java à analyser
        :param project_root: Racine du projet (pour les chemins relatifs)
        :param out: Liste de sortie où ajouter les fonctions trouvées
        """
        try:
            lines = java_file.read_text(encoding="utf-8", errors="replace").splitlines()
        except Exception:
            return

        content = "\n".join(lines)
        class_match = re.search(r"class\s+(\w+)", content)
        class_name = class_match.group(1) if class_match else "Unknown"

        # Pattern plus strict: doit commencer avec un modificateur réel (pas juste un espace)
        # et exclure les mots-clés de contrôle (if, for, while, switch, catch)
        func_pattern = re.compile(
            r"^\s*(?:public|private|protected|static|final|native|synchronized|abstract)\s+"
            r"(?:(?:public|private|protected|static|final|native|synchronized|abstract)\s+)*"
            r"[\w<>\[\], ?]+\s+(\w+)\s*\([^)]*\)\s*(?:throws\s+[^{]+)?\s*\{"
        )
        
        # Mots-clés de contrôle à exclure
        control_keywords = {"if", "for", "while", "switch", "catch", "synchronized"}

        for index, line in enumerate(lines):
            # Ignorer les lignes commentées
            stripped = line.strip()
            if stripped.startswith("//") or stripped.startswith("/*") or stripped.startswith("*"):
                continue
                
            match = func_pattern.search(line)
            if not match:
                continue
                
            func_name = match.group(1)
            # Exclure les mots-clés de contrôle
            if func_name in control_keywords:
                continue
                
            if self._has_javadoc(lines, index):
                continue

            out.append(
                {
                    "file": str(java_file.relative_to(project_root)),
                    "line": index + 1,
                    "class_name": class_name,
                    "function_name": func_name,
                    "signature": self._extract_function_signature(lines, index),
                    "code": self._extract_function_body(lines, index),
                }
            )

    def _has_javadoc(self, lines: Sequence[str], func_line: int) -> bool:
        """
        Détecte si une fonction a une Javadoc au-dessus d'elle.
        Remonte depuis la ligne de la fonction en sautant les annotations et lignes vides.

        :param lines: Liste des lignes du fichier source Java
        :param func_line: Numéro de ligne où se trouve la déclaration de la fonction
        :return: True si une Javadoc est présente, False sinon
        """
        i = func_line - 1
        
        # Sauter les lignes vides
        while i >= 0 and lines[i].strip() == "":
            i -= 1

        if i < 0:
            return False

        # Sauter les annotations (@Override, @Deprecated, etc.)
        while i >= 0 and lines[i].strip().startswith("@"):
            i -= 1
            # Sauter les lignes vides entre les annotations
            while i >= 0 and lines[i].strip() == "":
                i -= 1

        if i < 0:
            return False

        # Vérifier si on a bien une fin de commentaire
        stripped = lines[i].strip()
        if not stripped.endswith("*/"):
            return False

        # Remonter pour trouver le début du commentaire
        while i >= 0:
            stripped = lines[i].strip()
            if stripped.startswith("/**"):
                # C'est bien une Javadoc
                return True
            if stripped.startswith("/*") and not stripped.startswith("/**"):
                # C'est un commentaire simple, pas une Javadoc
                return False
            i -= 1
        
        return False

    def _extract_function_signature(self, lines: Sequence[str], start: int) -> str:
        """
        Extrait la signature complète d'une fonction Java.

        :param lines: Lignes du fichier source
        :param start: Index de la ligne de début de la fonction
        :return: Signature de la fonction sur une seule ligne
        """
        parts: List[str] = []
        i = start
        while i < len(lines) and i < start + 10:
            parts.append(lines[i].strip())
            if "{" in lines[i]:
                break
            i += 1
        return " ".join(parts).strip()

    def _extract_function_body(self, lines: Sequence[str], start: int, max_lines: int = 80) -> str:
        """
        Extrait le corps complet d'une fonction Java.

        :param lines: Lignes du fichier source
        :param start: Index de la ligne de début de la fonction
        :param max_lines: Nombre maximum de lignes à extraire
        :return: Corps de la fonction incluant la signature
        """
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
        """
        Génère un commentaire Javadoc pour une fonction Java via l'IA.

        :param model: Nom du modèle Ollama à utiliser
        :param function_code: Code source de la fonction
        :param class_name: Nom de la classe contenant la fonction
        :param function_name: Nom de la fonction
        :return: Bloc Javadoc formaté (/** ... */)
        """
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
        """
        Sélectionne le modèle de génération requis (qwen3:8b).

        :return: Nom du modèle si disponible, None sinon
        """
        required_model = "qwen3:8b"
        try:
            model_names = [m.name for m in self.list_models()]
        except OllamaError:
            return None

        if required_model in model_names:
            return required_model
        return None

    def _display_preview(
        self, 
        func_info: Dict[str, Any], 
        javadoc: str, 
        lines: List[str],
        index: int
    ) -> None:
        """
        Affiche un aperçu de la modification proposée dans le terminal.

        :param func_info: Dictionnaire contenant les informations de la fonction (file, line, class_name, function_name)
        :param javadoc: Bloc Javadoc généré à afficher
        :param lines: Liste des lignes du fichier source
        :param index: Index de la ligne de la fonction dans le fichier
        """
        print("\n" + "="*80)
        print(f" {func_info['file']}:{func_info['line']}")
        print(f" {func_info['class_name']}.{func_info['function_name']}")
        print("="*80)
        
        # Afficher le contexte avant
        start = max(0, func_info['line'] - 5)
        end = func_info['line']
        print(f"\n Contexte (lignes {start+1}-{end}):")
        for i in range(start, end):
            print(f"  {i+1:4d} | {lines[i]}")
        
        # Afficher la Javadoc générée
        print(f"\n Javadoc générée:")
        for line in javadoc.splitlines():
            print(f"  \033[32m+    | {line}\033[0m")
        
        # Afficher la fonction
        print(f"\n  {func_info['line']:4d} | {lines[func_info['line']-1]}")
        print()

    def _ask_user_validation(self, current: int, total: int) -> str:
        """
        Demande la validation à l'utilisateur pour une Javadoc générée.

        :param current: Numéro de la fonction en cours de traitement
        :param total: Nombre total de fonctions à traiter
        :return: Choix de l'utilisateur ('v' valider, 'r' rejeter, 'g' régénérer, 'q' quitter)
        """
        while True:
            print(f"[{current}/{total}] Choix:")
            print("  [v] Valider et appliquer")
            print("  [r] Rejeter (ne pas appliquer)")
            print("  [g] Régénérer avec l'IA")
            print("  [q] Quitter la génération")
            
            choice = input("\nVotre choix [v/r/g/q]: ").strip().lower()
            
            if choice in ['v', 'r', 'g', 'q']:
                return choice
            
            print(" Choix invalide. Veuillez entrer 'v', 'r', 'g' ou 'q'.")
            print()

    def auto_document_java_files(
        self,
        *,
        project_root: Union[str, Path] = ".",
        model: Optional[str] = None,
        dry_run: bool = False,
        interactive: bool = False,
    ) -> Dict[str, Any]:
        """
        Documente automatiquement toutes les fonctions Java sans Javadoc.

        :param project_root: Racine du projet à scanner
        :param model: Modèle à utiliser (par défaut: qwen3:8b)
        :param dry_run: Si True, prévisualise sans modifier les fichiers
        :param interactive: Si True, demande validation pour chaque Javadoc
        :return: Statistiques de l'opération (fonctions trouvées, documentées, erreurs)
        """
        root = Path(project_root)
        stats: Dict[str, Any] = {
            "total_scanned": 0,
            "functions_found": 0,
            "functions_documented": 0,
            "functions_failed": 0,
            "functions_rejected": 0,
            "files_modified": [],
            "errors": [],
        }

        model_name = model or self._select_generation_model()
        if not model_name:
            stats["errors"].append("Le modèle requis qwen3:8b est indisponible (installez-le avec: ollama pull qwen3:8b)")
            return stats

        if model_name != "qwen3:8b":
            stats["errors"].append("Le générateur impose le modèle qwen3:8b")
            return stats

        undocumented = self.detect_undocumented_functions(project_root=root)
        stats["functions_found"] = len(undocumented)
        if not undocumented:
            return stats

        by_file: Dict[str, List[Dict[str, Any]]] = {}
        for func in undocumented:
            by_file.setdefault(func["file"], []).append(func)

        # Mode interactif : traiter toutes les fonctions une par une
        if interactive:
            return self._interactive_documentation(
                root, undocumented, model_name, dry_run, stats
            )
        
        # Mode batch (ancien comportement)
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

    def _interactive_documentation(
        self,
        root: Path,
        undocumented: List[Dict[str, Any]],
        model_name: str,
        dry_run: bool,
        stats: Dict[str, Any]
    ) -> Dict[str, Any]:
        """
        Traite les fonctions en mode interactif avec validation utilisateur.

        :param root: Chemin racine du projet
        :param undocumented: Liste des fonctions sans documentation à traiter
        :param model_name: Nom du modèle Ollama à utiliser pour la génération
        :param dry_run: Si True, ne pas appliquer les modifications
        :param stats: Dictionnaire de statistiques à mettre à jour
        :return: Statistiques mises à jour de l'opération
        """
        total = len(undocumented)
        modifications: Dict[str, List[tuple]] = {}  # file -> [(line_index, javadoc, indent)]
        
        for i, func in enumerate(undocumented, 1):
            file_path = root / func["file"]
            
            if not file_path.exists():
                stats["errors"].append(f"Fichier non trouvé: {func['file']}")
                continue
            
            try:
                lines = file_path.read_text(encoding="utf-8", errors="replace").splitlines()
            except Exception as e:
                stats["errors"].append(f"Lecture impossible {func['file']}: {e}")
                continue
            
            # Boucle pour permettre la régénération
            while True:
                try:
                    # Générer la Javadoc
                    javadoc = self.generate_javadoc_for_function(
                        model=model_name,
                        function_code=func["code"],
                        class_name=func["class_name"],
                        function_name=func["function_name"],
                    )
                    
                    # Afficher l'aperçu
                    self._display_preview(func, javadoc, lines, func["line"] - 1)
                    
                    # Demander validation
                    choice = self._ask_user_validation(i, total)
                    
                    if choice == 'v':
                        # Valider : enregistrer la modification
                        line_index = func["line"] - 1
                        indent = len(lines[line_index]) - len(lines[line_index].lstrip())
                        
                        modifications.setdefault(func["file"], []).append(
                            (func["line"], javadoc, indent)
                        )
                        
                        stats["functions_documented"] += 1
                        print(" Validé\n")
                        break  # Passer à la fonction suivante
                    
                    elif choice == 'r':
                        # Rejeter : ignorer cette fonction
                        stats["functions_rejected"] += 1
                        print("Rejeté\n")
                        break  # Passer à la fonction suivante
                    
                    elif choice == 'g':
                        # Régénérer : reboucler
                        print("Régénération...\n")
                        continue  # Reboucler pour régénérer
                    
                    elif choice == 'q':
                        # Quitter
                        print("Arrêt de la génération.\n")
                        stats["errors"].append("Arrêt manuel par l'utilisateur")
                        # Appliquer les modifications déjà validées
                        self._apply_modifications(root, modifications, dry_run, stats)
                        return stats
                
                except Exception as e:
                    stats["functions_failed"] += 1
                    stats["errors"].append(f"{func['file']}:{func['line']} {e}")
                    print(f"Erreur: {e}\n")
                    break 
        
        # Appliquer toutes les modifications validées
        self._apply_modifications(root, modifications, dry_run, stats)
        return stats

    def _apply_modifications(
        self,
        root: Path,
        modifications: Dict[str, List[tuple]],
        dry_run: bool,
        stats: Dict[str, Any]
    ) -> None:
        """
        Applique toutes les modifications validées aux fichiers.

        :param root: Chemin racine du projet
        :param modifications: Dictionnaire {fichier: [(ligne, javadoc, indentation), ...]}
        :param dry_run: Si True, affiche un message sans modifier les fichiers
        :param stats: Dictionnaire de statistiques à mettre à jour
        """
        if dry_run:
            print("\n[DRY-RUN] Aucune modification appliquée.")
            return
        
        for relative_file, mods in modifications.items():
            file_path = root / relative_file
            stats["total_scanned"] += 1
            
            try:
                lines = file_path.read_text(encoding="utf-8", errors="replace").splitlines()
            except Exception as e:
                stats["errors"].append(f"Lecture impossible {relative_file}: {e}")
                continue
            
            # Trier par ligne décroissante pour insérer du bas vers le haut
            for line_num, javadoc, indent in sorted(mods, key=lambda x: x[0], reverse=True):
                line_index = line_num - 1
                prefix = " " * indent
                block = [prefix + ln if ln.strip() else "" for ln in javadoc.splitlines()]
                lines[line_index:line_index] = block + [""]
            
            file_path.write_text("\n".join(lines) + "\n", encoding="utf-8")
            stats["files_modified"].append(relative_file)
            print(f"💾 Fichier modifié: {relative_file}")

        return stats
