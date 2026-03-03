#!/bin/bash

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

CONFIG_FILE="config.properties"

read_config_value() {
    local key="$1"
    if [[ -f "$CONFIG_FILE" ]]; then
        grep -E "^${key}=" "$CONFIG_FILE" | tail -n1 | cut -d'=' -f2-
    fi
}

normalize_mode() {
    local value="$1"
    local upper
    upper="$(echo "$value" | tr '[:lower:]' '[:upper:]' | xargs)"
    if [[ "$upper" == "DEV" ]]; then
        echo "DEVELOPPEMENT"
    elif [[ "$upper" == "DEVELOPPEMENT" || "$upper" == "PRODUCTION" ]]; then
        echo "$upper"
    else
        echo "PRODUCTION"
    fi
}

MODE_OVERRIDE=""
for arg in "$@"; do
    case "$arg" in
        --mode=*)
            MODE_OVERRIDE="${arg#*=}"
            ;;
        --mode)
            shift
            MODE_OVERRIDE="$1"
            ;;
    esac
done

CONFIG_MODE="$(read_config_value "mode")"
if [[ -n "$MODE_OVERRIDE" ]]; then
    MODE="$(normalize_mode "$MODE_OVERRIDE")"
else
    MODE="$(normalize_mode "$CONFIG_MODE")"
fi

if [[ "$MODE" == "DEVELOPPEMENT" ]]; then
    echo -e "${BLUE}=== Lancement de la Borne d'Arcade - MODE DÉVELOPPEMENT ===${NC}"
else
    echo -e "${YELLOW}=== Lancement de la Borne d'Arcade - MODE PRODUCTION ===${NC}"
fi

export BORNE_MODE="$MODE"

echo -e "${YELLOW}Nettoyage...${NC}"
find lib -name "*.class" -delete 2>/dev/null || true
find src/main/java -name "*.class" -delete 2>/dev/null || true

echo -e "${YELLOW}Compilation des bibliothèques...${NC}"
javac -encoding UTF-8 lib/MG2D/*.java lib/MG2D/**/*.java 2>/dev/null || true

echo -e "${YELLOW}Compilation du code source...${NC}"
javac -encoding UTF-8 -cp ".:lib:src/resources" src/main/java/*.java

if [[ "$MODE" == "DEVELOPPEMENT" ]]; then
    DOC_ENABLED="$(read_config_value "dev.doc.enabled")"
    DOC_INTERACTIVE="$(read_config_value "dev.doc.interactive")"
    AUTO_DOC="$(read_config_value "dev.ai.auto_generate_doc")"
    echo -e "${BLUE}Mode DEV: doc.enabled=${DOC_ENABLED:-true}, doc.interactive=${DOC_INTERACTIVE:-false}, auto_generate=${AUTO_DOC:-true}${NC}"
fi

echo -e "${GREEN}Lancement de la borne d'arcade en mode ${MODE}...${NC}"
java -cp "lib:src/main/java:src/resources:src/projets" Main --mode="$MODE"
