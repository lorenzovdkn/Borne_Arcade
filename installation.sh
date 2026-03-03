#!/bin/bash
# Installation des dépendances pour la borne arcade

set -e

echo "=== Installation de la borne arcade ==="
echo ""

# Vérification root
if [ "$EUID" -eq 0 ]; then 
    echo "Erreur: Ne pas exécuter en tant que root"
    exit 1
fi

cd "$(dirname "${BASH_SOURCE[0]}")"

# Mise à jour système
echo "Mise à jour du système..."
sudo apt-get update
sudo apt-get upgrade -y

# Installation Java
echo "Installation de Java JDK 17..."
sudo apt-get install -y openjdk-17-jdk

# Installation Python
echo "Installation de Python 3 et pip..."
sudo apt-get install -y python3 python3-pip python3-dev

# Installation dépendances Pygame
echo "Installation des dépendances Pygame..."
sudo apt-get install -y libsdl2-dev libsdl2-image-dev libsdl2-mixer-dev libsdl2-ttf-dev \
    libfreetype6-dev libportmidi-dev libjpeg-dev python3-setuptools

# Installation Pygame
echo "Installation de Pygame..."
pip3 install --upgrade pygame

# Installation dépendances jeux Python
if [ -f "src/projets/ball-blast/requirements.txt" ]; then
    pip3 install -r src/projets/ball-blast/requirements.txt
fi
if [ -f "src/projets/TronGame/requirements.txt" ]; then
    pip3 install -r src/projets/TronGame/requirements.txt
fi

# Installation Love2D
echo "Installation de Love2D..."
sudo apt-get install -y love

# Installation Git
echo "Installation de Git..."
sudo apt-get install -y git

# Compilation bibliothèques Java
echo "Compilation des bibliothèques Java..."
find lib -name "*.class" -delete 2>/dev/null || true
find src -name "*.class" -delete 2>/dev/null || true

javac -encoding UTF-8 lib/MG2D/*.java lib/MG2D/**/*.java 2>/dev/null || true
javac -encoding UTF-8 lib/javazoom/jl/**/*.java 2>/dev/null || true
javac -encoding UTF-8 -cp ".:lib:src/resources" src/main/java/*.java

# Compilation jeux Java
for game in src/projets/*/; do
    if ls "$game"*.java 1> /dev/null 2>&1; then
        javac -encoding UTF-8 -cp "lib:$game:lib/MG2D" "$game"*.java 2>/dev/null || true
    fi
done

# Permissions
chmod +x launch.sh launch-arcade.sh launch-dev.sh installation.sh 2>/dev/null || true
find src/projets -name "*.sh" -exec chmod +x {} \; 2>/dev/null || true

echo ""
echo "=== INSTALLATION TERMINÉE ==="
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
