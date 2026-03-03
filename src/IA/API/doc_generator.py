#!/usr/bin/env python3

import argparse
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))

from ollama_wrapper import OllamaWrapper


def print_stats(stats):
    print(f"\nFichiers scannés: {stats['total_scanned']}")
    print(f"Fonctions sans doc: {stats['functions_found']}")
    print(f"Documentées: {stats['functions_documented']}")
    print(f"Rejetées: {stats.get('functions_rejected', 0)}")
    print(f"Échecs: {stats['functions_failed']}")

    if stats["files_modified"]:
        print("Fichiers modifiés:")
        for path in stats["files_modified"]:
            print(f"- {path}")

    if stats["errors"]:
        print("Erreurs:")
        for error in stats["errors"]:
            print(f"- {error}")


def run_check(client: OllamaWrapper, project_root: Path):
    undocumented = client.detect_undocumented_functions(project_root=project_root)
    if not undocumented:
        print("Aucune fonction sans Javadoc.")
        return

    print(f"{len(undocumented)} fonction(s) sans Javadoc:")
    for func in undocumented:
        print(f"- {func['class_name']}.{func['function_name']} ({func['file']}:{func['line']})")


def run_documentation(client: OllamaWrapper, project_root: Path, apply_changes: bool, interactive: bool):
    if not client.is_server_running():
        print("Serveur Ollama non accessible.")
        print("Lancez: ollama serve")
        sys.exit(1)

    if interactive and not apply_changes:
        print("⚠️  Mode interactif activé : les modifications seront appliquées après validation.")
        apply_changes = True  # En mode interactif, on applique forcément

    mode = "interactif" if interactive else ("apply" if apply_changes else "dry-run")
    print(f"Mode: {mode} (Ollama {client.get_version()})\n")

    stats = client.auto_document_java_files(
        project_root=project_root,
        dry_run=not apply_changes,
        interactive=interactive,
    )
    print_stats(stats)


def main():
    parser = argparse.ArgumentParser(description="Génération automatique de Javadoc")
    parser.add_argument("--check", action="store_true", help="Lister les fonctions non documentées")
    parser.add_argument("--apply", action="store_true", help="Appliquer les modifications")
    parser.add_argument("--interactive", "-i", "--i", action="store_true", help="Mode interactif avec validation manuelle")
    args = parser.parse_args()

    client = OllamaWrapper()
    project_root = Path(__file__).resolve().parents[3]

    if args.check:
        run_check(client, project_root)
    else:
        run_documentation(client, project_root, apply_changes=args.apply, interactive=args.interactive)


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("Interrompu.")
        sys.exit(1)
