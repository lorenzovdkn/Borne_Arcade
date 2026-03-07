# Documentation Technique - Borne d'Arcade

## Table des Matières

1. [Architecture du Projet](#architecture-du-projet)
2. [Technologies Utilisées](#technologies-utilisées)
3. [Structure des Fichiers](#structure-des-fichiers)
4. [Composants Principaux](#composants-principaux)
5. [Système de Lancement de Jeux](#système-de-lancement-de-jeux)
6. [Configuration](#configuration)
7. [Bibliothèque MG2D](#bibliothèque-mg2d)
8. [Système de Highscores](#système-de-highscores)
9. [Générateur de Documentation IA](#générateur-de-documentation-ia)

---

## Architecture du Projet

```
borne_arcade/
├── src/
│   ├── main/java/          # Code source principal de la borne
│   ├── projets/            # Dossiers des jeux
│   ├── resources/          # Ressources (polices, images, sons)
│   └── tests/              # Tests automatisés
├── lib/
│   └── MG2D/               # Bibliothèque graphique Java
├── Documents/              # Documentation
├── config.properties       # Fichier de configuration
├── installation.sh         # Script d'installation
├── launch.sh               # Script de lancement
└── launch-arcade.sh        # Script de lancement mode arcade
```

---

## Technologies Utilisées

| Technologie | Version | Utilisation |
|-------------|---------|-------------|
| Java | JDK 17+ | Application principale, jeux Java |
| Python 3 | 3.x | Jeux Python (ball-blast, TronGame, etc.) |
| Love2D | 11.x | Jeux Lua (CursedWare) |
| MG2D | - | Bibliothèque graphique 2D Java |
| Pygame | - | Framework pour jeux Python |

### Dépendances Python
- `pygame` : Framework de jeux 2D
- `librosa` : Analyse audio (utilisé par PianoTile)
- Autres selon les `requirements.txt` de chaque projet

---

## Structure des Fichiers

### Fichiers Racine

| Fichier | Description |
|---------|-------------|
| `config.properties` | Configuration de l'application (mode, options) |
| `installation.sh` | Script d'installation des dépendances |
| `launch.sh` | Script principal de lancement |
| `launch-arcade.sh` | Lancement direct mode production |
| `launch-dev.sh` | Lancement mode développement |
| `borne.desktop` | Fichier autostart pour Raspberry Pi |

### Code Source Principal (`src/main/java/`)

| Classe | Description |
|--------|-------------|
| `Main.java` | Point d'entrée de l'application |
| `Graphique.java` | Interface graphique principale |
| `GameLauncher.java` | Détection et lancement des jeux |
| `Bouton.java` | Représentation d'un jeu dans le menu |
| `Config.java` | Gestion de la configuration |
| `ClavierBorneArcade.java` | Gestion des entrées clavier |
| `BoiteSelection.java` | Composant de sélection des jeux |
| `BoiteDescription.java` | Affichage des descriptions |
| `BoiteImage.java` | Affichage des images |
| `HighScore.java` | Gestion des scores |
| `Pointeur.java` | Pointeur de sélection |

---

## Système de Lancement de Jeux

### Détection du Type de Jeu
```java
public static GameType detectGameType(File gameDir) {
    // Priorité 1 : Shell scripts
    if (hasShellScript(gameDir)) return GameType.SHELL;
    
    // Priorité 2 : Love2D
    if (hasLuaFiles(gameDir)) return GameType.LOVE;
    
    // Priorité 3 : Python
    if (hasPythonFiles(gameDir)) return GameType.PYTHON;
    
    // Priorité 4 : Java
    if (hasJavaFiles(gameDir)) return GameType.JAVA;
    
    return GameType.UNKNOWN;
}
```

### Commandes de Lancement par Type

| Type | Commande |
|------|----------|
| SHELL | `/bin/bash ./{script}.sh` |
| PYTHON | `python3 ./src` ou `python3 main.py` |
| LOVE | `love .` |
| JAVA | `java -cp ... Main` |

---

## Configuration

### config.properties
```properties
# Mode de l'application
mode=PRODUCTION              # PRODUCTION ou DEVELOPPEMENT

# Options développement
dev.ai.enabled=false         # Activer l'IA
dev.doc.enabled=true         # Générer documentation
dev.doc.interactive=true     # Mode interactif

# Options production
prod.fullscreen=true         # Plein écran
prod.debug=false             # Mode debug
```

### Arguments de Lancement
```bash
# Mode production (défaut)
./launch.sh

# Mode développement
./launch.sh --dev
./launch-dev.sh
```

---

## Système de Highscores

### Format du Fichier `highscore`
```
ABC-1000
DEF-750
GHI-500
```

Format : `NOM-SCORE` (3 caractères pour le nom)

### Fonctionnement
1. À la fin du jeu, vérification si le score entre dans le top 10
2. Interface de saisie du nom (3 caractères)
3. Navigation avec flèches haut/bas pour les lettres
4. Sauvegarde dans le fichier `highscore`

---

## Mapping Clavier Borne

### Joueur 1
| Contrôle | Touche |
|----------|--------|
| Joystick Haut | ↑ |
| Joystick Bas | ↓ |
| Joystick Gauche | ← |
| Joystick Droite | → |
| Bouton A | R |
| Bouton B | T |
| Bouton C | Y |
| Bouton X | F |
| Bouton Y | G |
| Bouton Z | H |

### Joueur 2
| Contrôle | Touche |
|----------|--------|
| Joystick Haut | O |
| Joystick Bas | L |
| Joystick Gauche | K |
| Joystick Droite | M |
| Bouton A | A |
| Bouton B | Z |
| Bouton C | E |
| Bouton X | Q |
| Bouton Y | S |
| Bouton Z | D |

---

## Compilation

### Compilation Manuelle
```bash
# Bibliothèque MG2D
find lib/MG2D -name "*.java" -print0 | xargs -0 javac -encoding UTF-8 -cp lib

# Application principale
javac -encoding UTF-8 -cp ".:lib:src/resources" src/main/java/*.java

# Un jeu spécifique
cd src/projets/NomDuJeu
javac -encoding UTF-8 -cp ".:../../main/java:../../../lib" *.java
```

### Via Script d'Installation
```bash
./installation.sh
```

---

## Générateur de Documentation IA

Le projet inclut un outil de génération automatique de Javadoc utilisant l'IA via l'API Ollama.

### Présentation

| Élément | Description |
|---------|-------------|
| **Emplacement** | `src/IA/API/doc_generator.py` |
| **Serveur IA** | Ollama hébergé à l'IUT (`10.22.28.190:11434`) |
| **Modèle requis** | `qwen3:8b` |
| **Langage cible** | Java (génération de Javadoc) |

### Architecture

```
src/IA/API/
├── doc_generator.py    # Script principal CLI
└── ollama_wrapper.py   # Wrapper pour l'API Ollama
```

### Utilisation

#### Vérifier les fonctions sans documentation
```bash
python3 src/IA/API/doc_generator.py --check
```
Affiche la liste des fonctions Java sans Javadoc.

#### Mode Dry-Run (prévisualisation)
```bash
python3 src/IA/API/doc_generator.py
```
Génère les Javadoc mais ne modifie pas les fichiers. Affiche les statistiques.

#### Mode Application Automatique
```bash
python3 src/IA/API/doc_generator.py --apply
```
Génère et applique automatiquement les Javadoc aux fichiers.

#### Mode Interactif (recommandé)
```bash
python3 src/IA/API/doc_generator.py --interactive
# ou
python3 src/IA/API/doc_generator.py -i
```
Pour chaque fonction, affiche un aperçu et demande validation :
- `[v]` Valider et appliquer
- `[r]` Rejeter (ne pas appliquer)
- `[g]` Régénérer avec l'IA
- `[q]` Quitter

### Options de la Ligne de Commande

| Option | Description |
|--------|-------------|
| `--check` | Liste les fonctions sans Javadoc (sans générer) |
| `--apply` | Applique les modifications aux fichiers |
| `--interactive`, `-i` | Mode interactif avec validation manuelle |

### Configuration via config.properties

```properties
# Activer/désactiver l'IA
dev.ai.enabled=false

# Génération automatique au lancement (mode dev)
dev.ai.auto_generate_doc=true

# Activer la génération de doc
dev.doc.enabled=true

# Mode interactif par défaut
dev.doc.interactive=true

# Appliquer automatiquement (si non interactif)
dev.doc.apply=false

# Dossier de sortie pour la doc générée
dev.doc.output_dir=src/docs/generated
```

### Serveur Ollama de l'IUT

Le serveur Ollama est hébergé localement à l'IUT :

| Paramètre | Valeur |
|-----------|--------|
| **Adresse** | `10.22.28.190` |
| **Port** | `11434` |
| **URL complète** | `http://10.22.28.190:11434` |

**Note** : Ce serveur n'est accessible que depuis le réseau de l'IUT.

### Exemple de Sortie

```
Mode: interactif (Ollama 0.5.1)

================================================================================
📄 src/main/java/Bouton.java:67
📌 Bouton.remplirBouton
================================================================================

📖 Contexte (lignes 63-67):
    63 |     }
    64 | 
    65 |     @Override
    66 |     public String toString() {
    67 |     public static void remplirBouton(){

✨ Javadoc générée:
  +    | /**
  +    |  * Remplit automatiquement le tableau de boutons avec les jeux disponibles.
  +    |  * Parcourt le répertoire "projet/" et crée un bouton pour chaque jeu trouvé.
  +    |  */

    67 | public static void remplirBouton(){

[1/5] Choix:
  [v] Valider et appliquer
  [r] Rejeter (ne pas appliquer)
  [g] Régénérer avec l'IA
  [q] Quitter la génération

Votre choix [v/r/g/q]: v
```

### Statistiques Générées

À la fin de l'exécution, le script affiche :

```
Fichiers scannés: 12
Fonctions sans doc: 8
Documentées: 6
Rejetées: 1
Échecs: 1
Fichiers modifiés:
- src/main/java/Bouton.java
- src/main/java/Graphique.java
```

### Dépannage

| Problème | Solution |
|----------|----------|
| "Serveur Ollama non accessible" | Vérifiez la connexion au réseau IUT |
| "Modèle qwen3:8b indisponible" | Contactez l'administrateur du serveur |
| Timeout lors de la génération | Le serveur est surchargé, réessayez |
| Javadoc incorrecte | Utilisez le mode interactif avec `[g]` pour régénérer |

---

## Auteurs et Versions

- **Version Originale** : Romaric Bougard, Pierre Delobel, Bastien Ducloy (2017-2018)
- **Améliorations** : Lorenzo Vandenkoornhuyse (2026)
- **Matériel cible** : Raspberry Pi 3+, Écran 1280x1024
