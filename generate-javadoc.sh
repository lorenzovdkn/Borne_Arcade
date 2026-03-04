#!/bin/bash

# =============================================================================
# Script de génération de Javadoc
# Utilise le système IA pour auto-générer les commentaires manquants
# =============================================================================

set -e

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

# Répertoire du script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Configuration par défaut
CONFIG_FILE="config.properties"
IA_SCRIPT="src/IA/API/doc_generator.py"
JAVADOC_OUTPUT="src/docs/generated"
JAVA_SRC_DIRS="src/main/java"
LIB_DIR="lib"

# Options
DRY_RUN=false
APPLY=false
CHECK_ONLY=false
INTERACTIVE=false
GENERATE_HTML=false
USE_IA=false
VERBOSE=false
HELP=false

# =============================================================================
# Fonctions utilitaires
# =============================================================================

print_header() {
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC}${BOLD}           Générateur de Javadoc - Borne d'Arcade              ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════════════╝${NC}"
    echo
}

print_help() {
    print_header
    echo -e "${BOLD}UTILISATION:${NC}"
    echo "  ./generate-javadoc.sh [OPTIONS]"
    echo
    echo -e "${BOLD}MODES DE FONCTIONNEMENT:${NC}"
    echo -e "  ${CYAN}--check${NC}              Analyser les fichiers et lister les fonctions sans documentation"
    echo -e "  ${CYAN}--dry-run${NC}            Générer la doc avec l'IA sans appliquer les modifications"
    echo -e "  ${CYAN}--apply${NC}              Générer et appliquer les modifications de documentation IA"
    echo -e "  ${CYAN}--interactive, -i${NC}    Mode interactif avec validation manuelle de chaque commentaire"
    echo -e "  ${CYAN}--javadoc${NC}            Générer la documentation HTML avec javadoc"
    echo
    echo -e "${BOLD}OPTIONS:${NC}"
    echo -e "  ${CYAN}--output=DIR${NC}         Répertoire de sortie pour la doc HTML (défaut: $JAVADOC_OUTPUT)"
    echo -e "  ${CYAN}--src=DIR${NC}            Répertoire source Java (défaut: $JAVA_SRC_DIRS)"
    echo -e "  ${CYAN}--verbose, -v${NC}        Mode verbeux"
    echo -e "  ${CYAN}--help, -h${NC}           Afficher cette aide"
    echo
    echo -e "${BOLD}EXEMPLES:${NC}"
    echo -e "  ${GREEN}# Vérifier les fonctions sans documentation${NC}"
    echo "  ./generate-javadoc.sh --check"
    echo
    echo -e "  ${GREEN}# Prévisualiser les commentaires générés par l'IA${NC}"
    echo "  ./generate-javadoc.sh --dry-run"
    echo
    echo -e "  ${GREEN}# Générer et appliquer les commentaires manquants${NC}"
    echo "  ./generate-javadoc.sh --apply"
    echo
    echo -e "  ${GREEN}# Mode interactif : valider chaque commentaire manuellement${NC}"
    echo "  ./generate-javadoc.sh --interactive"
    echo
    echo -e "  ${GREEN}# Générer la documentation HTML${NC}"
    echo "  ./generate-javadoc.sh --javadoc"
    echo
    echo -e "  ${GREEN}# Générer les commentaires puis la doc HTML${NC}"
    echo "  ./generate-javadoc.sh --apply --javadoc"
    echo
    echo -e "${BOLD}PRÉREQUIS POUR L'IA:${NC}"
    echo "  - Python 3.x"
    echo "  - Serveur Ollama accessible"
    echo "  - Modèle qwen3:8b installé (ollama pull qwen3:8b)"
    echo
}

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[OK]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERREUR]${NC} $1"
}

check_python() {
    if command -v python3 &> /dev/null; then
        PYTHON_CMD="python3"
    elif command -v python &> /dev/null; then
        PYTHON_CMD="python"
    else
        log_error "Python non trouvé. Installez Python 3 pour utiliser les fonctionnalités IA."
        return 1
    fi
    
    if [[ "$VERBOSE" == true ]]; then
        log_info "Python trouvé: $($PYTHON_CMD --version)"
    fi
    return 0
}

check_javadoc() {
    if ! command -v javadoc &> /dev/null; then
        log_error "javadoc non trouvé. Installez le JDK pour générer la documentation HTML."
        return 1
    fi
    
    if [[ "$VERBOSE" == true ]]; then
        log_info "javadoc trouvé: $(javadoc --version 2>&1 | head -1 || echo 'version inconnue')"
    fi
    return 0
}

check_ia_script() {
    if [[ ! -f "$IA_SCRIPT" ]]; then
        log_error "Script IA non trouvé: $IA_SCRIPT"
        return 1
    fi
    return 0
}

read_config_value() {
    local key="$1"
    if [[ -f "$CONFIG_FILE" ]]; then
        grep -E "^${key}=" "$CONFIG_FILE" 2>/dev/null | tail -n1 | cut -d'=' -f2-
    fi
}

# =============================================================================
# Fonctions principales
# =============================================================================

run_check() {
    log_info "Analyse des fichiers pour détecter les fonctions sans documentation..."
    echo
    
    if ! check_python; then
        exit 1
    fi
    
    if ! check_ia_script; then
        exit 1
    fi
    
    $PYTHON_CMD "$IA_SCRIPT" --check
}

run_dry_run() {
    log_info "Mode dry-run : prévisualisation des commentaires générés par l'IA"
    log_warning "Aucune modification ne sera appliquée"
    echo
    
    if ! check_python; then
        exit 1
    fi
    
    if ! check_ia_script; then
        exit 1
    fi
    
    $PYTHON_CMD "$IA_SCRIPT"
}

run_apply() {
    log_info "Application des commentaires générés par l'IA..."
    echo
    
    if ! check_python; then
        exit 1
    fi
    
    if ! check_ia_script; then
        exit 1
    fi
    
    $PYTHON_CMD "$IA_SCRIPT" --apply
}

run_interactive() {
    log_info "Mode interactif : validation manuelle de chaque commentaire"
    echo
    
    if ! check_python; then
        exit 1
    fi
    
    if ! check_ia_script; then
        exit 1
    fi
    
    $PYTHON_CMD "$IA_SCRIPT" --interactive
}

run_javadoc() {
    log_info "Génération de la documentation HTML avec javadoc..."
    
    if ! check_javadoc; then
        exit 1
    fi
    
    # Créer le répertoire de sortie
    mkdir -p "$JAVADOC_OUTPUT"
    
    # Collecter tous les fichiers Java
    local java_files=()
    
    # Fichiers sources principaux uniquement (exclure les libs)
    if [[ -d "$JAVA_SRC_DIRS" ]]; then
        while IFS= read -r -d '' file; do
            java_files+=("$file")
        done < <(find "$JAVA_SRC_DIRS" -name "*.java" -print0 2>/dev/null)
    fi
    
    # Note: Les bibliothèques (MG2D, etc.) sont exclues de la génération
    
    if [[ ${#java_files[@]} -eq 0 ]]; then
        log_warning "Aucun fichier Java trouvé"
        return 1
    fi
    
    log_info "Fichiers trouvés: ${#java_files[@]}"
    
    # Options javadoc
    local javadoc_opts=(
        -d "$JAVADOC_OUTPUT"
        -encoding UTF-8
        -docencoding UTF-8
        -charset UTF-8
        -windowtitle "Borne d'Arcade - Documentation"
        -doctitle "Borne d'Arcade - Documentation API"
        -header "Borne d'Arcade"
        -author
        -version
        -private
        -sourcepath "$JAVA_SRC_DIRS"
        -classpath ".:$LIB_DIR"
    )
    
    # Mode verbeux ou silencieux
    if [[ "$VERBOSE" != true ]]; then
        javadoc_opts+=(-quiet)
    fi
    
    echo
    log_info "Exécution de javadoc..."
    
    if javadoc "${javadoc_opts[@]}" "${java_files[@]}" 2>&1; then
        echo
        log_success "Documentation générée dans: $JAVADOC_OUTPUT"
        log_info "Ouvrez $JAVADOC_OUTPUT/index.html dans un navigateur"
    else
        echo
        log_warning "javadoc a terminé avec des avertissements"
        log_info "Documentation générée dans: $JAVADOC_OUTPUT"
    fi
}

# =============================================================================
# Parsing des arguments
# =============================================================================

parse_args() {
    while [[ $# -gt 0 ]]; do
        case "$1" in
            --help|-h)
                HELP=true
                ;;
            --check)
                CHECK_ONLY=true
                ;;
            --dry-run)
                DRY_RUN=true
                USE_IA=true
                ;;
            --apply)
                APPLY=true
                USE_IA=true
                ;;
            --interactive|-i)
                INTERACTIVE=true
                USE_IA=true
                ;;
            --javadoc)
                GENERATE_HTML=true
                ;;
            --verbose|-v)
                VERBOSE=true
                ;;
            --output=*)
                JAVADOC_OUTPUT="${1#*=}"
                ;;
            --src=*)
                JAVA_SRC_DIRS="${1#*=}"
                ;;
            *)
                log_warning "Option inconnue: $1"
                ;;
        esac
        shift
    done
}

# =============================================================================
# Main
# =============================================================================

main() {
    parse_args "$@"
    
    if [[ "$HELP" == true ]]; then
        print_help
        exit 0
    fi
    
    print_header
    
    # Si aucune option spécifiée, afficher l'aide
    if [[ "$CHECK_ONLY" != true && "$DRY_RUN" != true && "$APPLY" != true && "$INTERACTIVE" != true && "$GENERATE_HTML" != true ]]; then
        echo -e "${YELLOW}Aucune action spécifiée. Utilisez --help pour voir les options disponibles.${NC}"
        echo
        echo -e "Actions rapides:"
        echo -e "  ${CYAN}--check${NC}        Vérifier les fonctions sans doc"
        echo -e "  ${CYAN}--dry-run${NC}      Prévisualiser les docs IA"
        echo -e "  ${CYAN}--apply${NC}        Appliquer les docs IA"
        echo -e "  ${CYAN}--interactive${NC}  Mode interactif"
        echo -e "  ${CYAN}--javadoc${NC}      Générer HTML"
        echo
        exit 0
    fi
    
    # Exécuter les actions dans l'ordre logique
    
    # 1. Check (analyse uniquement)
    if [[ "$CHECK_ONLY" == true ]]; then
        run_check
    fi
    
    # 2. Génération IA (si pas check only)
    if [[ "$CHECK_ONLY" != true ]]; then
        if [[ "$INTERACTIVE" == true ]]; then
            run_interactive
        elif [[ "$APPLY" == true ]]; then
            run_apply
        elif [[ "$DRY_RUN" == true ]]; then
            run_dry_run
        fi
    fi
    
    # 3. Génération HTML javadoc
    if [[ "$GENERATE_HTML" == true ]]; then
        echo
        run_javadoc
    fi
    
    echo
    log_success "Terminé"
}

main "$@"
