#!/bin/bash
# Installation des dépendances pour la borne arcade
# Compatible avec Raspberry Pi OS (dernière version)

set -e

echo "=== Installation de la borne arcade ==="
echo ""

cd "$(dirname "${BASH_SOURCE[0]}")"

# Mise à jour système
echo "Mise à jour du système..."
sudo apt-get update
sudo apt-get upgrade -y

# Installation des dépendances de base
echo "Installation des dépendances de base..."
sudo apt-get install -y wget curl git ca-certificates

# Installation Java
echo "Installation de Java JDK 17..."
sudo apt-get install -y openjdk-17-jdk

# Installation Python
echo "Installation de Python 3 et pip..."
sudo apt-get install -y python3 python3-pip python3-dev python3-venv

# Afficher la version installée
echo "Python version:"
python3 --version
echo "Java version:"
java -version

# Installation dépendances Pygame
echo "Installation des dépendances Pygame..."
sudo apt-get install -y libsdl2-dev libsdl2-image-dev libsdl2-mixer-dev libsdl2-ttf-dev \
    libfreetype6-dev libportmidi-dev libjpeg-dev python3-setuptools

# Installation Pygame avec flag pour Raspberry Pi
echo "Installation de Pygame..."
pip3 install --upgrade --break-system-packages pygame

# Installation dépendances jeux Python
if [ -f "src/projets/ball-blast/requirements.txt" ]; then
    echo "Installation dépendances ball-blast..."
    pip3 install --break-system-packages -r src/projets/ball-blast/requirements.txt
fi
if [ -f "src/projets/TronGame/requirements.txt" ]; then
    echo "Installation dépendances TronGame..."
    pip3 install --break-system-packages -r src/projets/TronGame/requirements.txt
fi

# Installation Love2D
echo "Installation de Love2D..."
sudo apt-get install -y love

# Installation Git (déjà fait mais on s'assure)
echo "Installation de Git..."
sudo apt-get install -y git

# Compilation bibliothèques Java
echo "Compilation des bibliothèques Java..."
find lib -name "*.class" -delete 2>/dev/null || true
find src -name "*.class" -delete 2>/dev/null || true

echo "Compilation de MG2D et dépendances..."
find lib/MG2D -name "*.java" -print0 | xargs -0 javac -encoding UTF-8 -cp lib 2>/dev/null || true

echo "Compilation des classes principales..."
javac -encoding UTF-8 -cp ".:lib:src/resources" src/main/java/*.java 2>/dev/null || true

# Compilation jeux Java
echo "Compilation des jeux Java..."
for game in src/projets/*/; do
    if [ -d "$game" ] && ls "$game"*.java 1> /dev/null 2>&1; then
        gamename=$(basename "$game")
        echo "  Compilation de $gamename..."
        javac -encoding UTF-8 -cp "lib:$game" "$game"*.java 2>/dev/null || true
    fi
done

# Permissions
echo "Configuration des permissions..."
chmod +x launch.sh launch-arcade.sh launch-dev.sh installation.sh 2>/dev/null || true
find src/projets -name "*.sh" -exec chmod +x {} \; 2>/dev/null || true

echo ""
echo "=== INSTALLATION TERMINÉE AVEC SUCCÈS ==="
echo ""
echo "Lancement de la borne d'arcade:"
echo "   Mode production (par défaut):  ./launch.sh"
echo "   Mode production direct:        ./launch-arcade.sh"
echo "   Mode développement:            ./launch-dev.sh"
echo ""
echo "Configuration:"
echo "   Éditer config.properties pour personnaliser:"
echo "   - mode: PRODUCTION ou DEVELOPPEMENT"
echo "   - dev.doc.interactive: true ou false"
echo "   - dev.doc.enabled: true ou false"
echo ""
echo "Note: Ollama est disponible en serveur local à l'IUT (10.22.28.190:11434)"
echo ""
echo "La borne est maintenant prête à l'emploi!"
