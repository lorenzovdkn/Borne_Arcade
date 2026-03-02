#!/bin/bash

# Script de lancement de la borne d'arcade en MODE DEVELOPPEMENT
# Compile les bibliothèques et le code source puis lance l'application
# avec les outils de développement (IA, génération de documentation, etc.)

set -e  # Arrêt en cas d'erreur

# Couleurs pour les messages
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Lancement de la Borne d'Arcade - MODE DÉVELOPPEMENT ===${NC}"

# Se positionner à la racine du projet
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Nettoyage des anciens fichiers compilés
echo -e "${YELLOW}Nettoyage...${NC}"
find lib -name "*.class" -delete 2>/dev/null || true
find src/main/java -name "*.class" -delete 2>/dev/null || true

# Compilation des bibliothèques MG2D et javazoom
echo -e "${YELLOW}Compilation des bibliothèques...${NC}"
javac -encoding UTF-8 lib/MG2D/*.java lib/MG2D/**/*.java 2>/dev/null || true

# Compilation du code source principal
echo -e "${YELLOW}Compilation du code source...${NC}"
javac -encoding UTF-8 -cp ".:lib:src/resources" src/main/java/*.java

# Définir la variable d'environnement pour le mode développement
export BORNE_MODE=DEVELOPPEMENT

# Lancement de l'application en mode développement
echo -e "${GREEN}Lancement en mode DÉVELOPPEMENT...${NC}"
echo -e "${BLUE}Outils IA et génération de documentation activés${NC}"
echo ""
java -cp "lib:src/main/java:src/resources:src/projets" Main --mode=DEVELOPPEMENT
