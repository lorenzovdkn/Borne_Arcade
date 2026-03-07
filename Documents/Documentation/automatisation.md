# Automatisations du Projet Borne Arcade

Ce document résume les différentes automatisations mises en place dans le projet.

---

## 1. Script d'Installation (`installation.sh`)

### Objectif
Automatiser l'installation complète de l'environnement de la borne arcade sur Raspberry Pi OS.

### Ce qu'il fait

#### Mise à jour système
```bash
sudo apt-get update && sudo apt-get upgrade -y
```

#### Installation des dépendances

| Composant | Version/Paquet |
|-----------|----------------|
| Java | OpenJDK 21 |
| Python | Python 3 + pip |
| Pygame | Via pip |
| Love2D | Pour les jeux Lua |
| Git | Pour le versioning |

#### Dépendances spécifiques aux jeux
- **ball-blast** : Installation depuis `requirements.txt`
- **TronGame** : Installation depuis `requirements.txt`  
- **PianoTile** : Installation de `librosa`

#### Compilation automatique
1. Nettoyage des anciens fichiers `.class`
2. Compilation de la bibliothèque MG2D
3. Compilation des classes principales (`src/main/java/`)
4. Compilation de tous les jeux Java dans `src/projets/`

#### Configuration finale
- Attribution des permissions d'exécution aux scripts (`.sh`)
- Affichage des instructions de lancement

### Utilisation
```bash
./installation.sh
```

---

## 2. Scripts de Lancement (`launch.sh`)

### Objectif
Automatiser le lancement de la borne arcade avec gestion des modes et compilation automatique.

### Architecture des scripts

```
├── launch.sh           # Script principal intelligent
├── launch-arcade.sh    # Raccourci mode PRODUCTION
└── launch-dev.sh       # Raccourci mode DEVELOPPEMENT
```

### Fonctionnalités du script principal

#### Gestion des modes
Le script lit la configuration depuis `config.properties` et supporte deux modes :
- **PRODUCTION** : Pour l'utilisation sur la borne (plein écran, pas d'outils de dev)
- **DEVELOPPEMENT** : Avec options IA et génération de documentation

```bash
# Lancement par défaut (lit config.properties)
./launch.sh

# Forcer un mode spécifique
./launch.sh --mode=PRODUCTION
./launch.sh --mode=DEVELOPPEMENT
```

#### Compilation automatique à chaque lancement
 1. Nettoyage des anciens .class

 2. Compilation MG2D

 3. Compilation du code source

### Configuration (`config.properties`)

```properties
# Mode par défaut
mode=PRODUCTION

# Options mode développement
dev.ai.enabled=false
dev.ai.auto_generate_doc=true
dev.doc.enabled=true
dev.doc.interactive=true

# Options mode production
prod.fullscreen=true
prod.debug=false
```

---

## 3. Détection et Lancement Automatique des Jeux (`GameLauncher.java`)

### Objectif
Détecter automatiquement le type de chaque jeu et le lancer avec la bonne commande.

### Types de jeux supportés

| Type | Détection | Commande de lancement |
|------|-----------|----------------------|
| **SHELL** | Fichier `.sh` présent | `bash ./script.sh` |
| **LOVE** | `main.lua` ou `conf.lua` | `love .` |
| **PYTHON** | Dossier `src/` ou fichiers `.py` | `python3 ./src` ou `python3 main.py` |
| **JAVA** | Fichiers `.java` ou `.class` | `java Main` (avec compilation auto) |

### Algorithme de détection (par priorité)
```java
1. Scripts shell (.sh) → SHELL
2. Fichiers Lua (main.lua, conf.lua) → LOVE
3. Fichiers Python (src/, app/, .py) → PYTHON
4. Fichiers Java (.java, .class) → JAVA
5. Sinon → UNKNOWN
```

### Compilation automatique des jeux Java
Si un jeu Java n'est pas compilé, `GameLauncher` le compile automatiquement avant de le lancer

### Avantages
- **Plug & Play** : Ajouter un jeu = créer un dossier avec les fichiers
- **Pas de configuration** : Le type est détecté automatiquement
- **Multi-langage** : Java, Python, Lua, Shell supportés nativement

---

## 4. Génération Automatique de Javadoc via IA

### Objectif
Documenter automatiquement le code Java du projet en utilisant l'IA de l'IUT.

### Configuration technique

| Paramètre | Valeur |
|-----------|--------|
| Serveur Ollama | `http://10.22.28.190:11434` (serveur IUT) |
| Modèle IA | `qwen3:8b` |
| Timeout | 300 secondes |

### Fonctionnement

#### 1. Détection des fonctions non documentées
Le wrapper scanne les fichiers Java dans :
- `src/main/java/`
- `src/tests/java/`

Il détecte les fonctions publiques/privées/protégées sans bloc Javadoc (`/** ... */`).

#### 2. Génération de la documentation
Pour chaque fonction détectée :
1. Extraction du code de la fonction
2. Envoi à l'IA avec un prompt demandant une Javadoc en français
3. Formatage et insertion dans le fichier source

#### 3. Modes disponibles

| Mode | Commande | Description |
|------|----------|-------------|
| Check | `--check` | Liste les fonctions non documentées |
| Dry-run | (par défaut) | Prévisualise sans modifier |
| Apply | `--apply` | Applique les modifications |
| Interactif | `--interactive` ou `-i` | Validation manuelle de chaque Javadoc |
| HTML | `--javadoc` | Génère la doc HTML avec javadoc |

### Script Shell (`generate-javadoc.sh`)
Un wrapper shell complet avec interface colorée :

```bash
# Vérifier les fonctions sans documentation
./generate-javadoc.sh --check

# Prévisualiser les commentaires générés
./generate-javadoc.sh --dry-run

# Appliquer les modifications
./generate-javadoc.sh --apply

# Mode interactif
./generate-javadoc.sh --interactive

# Générer la doc HTML après avoir ajouté les commentaires
./generate-javadoc.sh --apply --javadoc
```

### Mode Interactif
Le mode interactif permet de valider chaque documentation générée :

```
================================================================================
src/main/java/Main.java:42
Main.initialiserMenu
================================================================================

Contexte (lignes 38-42):
    38 |     private Config config;
    39 |     
    40 |     @Override
    41 |     
    42 |     public boolean initialiserMenu(Config config) {

Javadoc générée:
  +    | /**
  +    |  * Initialise le menu principal de la borne arcade.
  +    |  * @param config La configuration de l'application
  +    |  * @return true si l'initialisation réussit
  +    |  */

[1/5] Choix:
  [v] Valider et appliquer
  [r] Rejeter (ne pas appliquer)
  [g] Régénérer avec l'IA
  [q] Quitter la génération
```

### Exemple de Javadoc générée

```java
/**
 * Initialise le menu principal de la borne arcade.
 * Configure les boutons et charge les ressources graphiques.
 * 
 * @param config La configuration de l'application
 * @return true si l'initialisation réussit, false sinon
 */
public boolean initialiserMenu(Config config) {
    // ...
}
```

## Résumé

| Automatisation | Fichier(s) | Fonction |
|----------------|------------|----------|
| Installation | `installation.sh` | Installation complète de l'environnement |
| Lancement | `launch.sh` | Compilation + lancement avec gestion des modes |
| Détection jeux | `GameLauncher.java` | Détection automatique du type et lancement |
| Documentation IA | `generate-javadoc.sh` + `src/IA/API/` | Génération de Javadoc via IA de l'IUT |