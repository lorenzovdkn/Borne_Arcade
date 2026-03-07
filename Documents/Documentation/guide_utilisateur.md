# Guide Utilisateur - Borne d'Arcade

## Table des Matières

1. [Introduction](#introduction)
2. [Installation](#installation)
3. [Premier Lancement](#premier-lancement)
4. [Utilisation du Menu](#utilisation-du-menu)
5. [Jouer](#jouer)
6. [Contrôles](#contrôles)
7. [Highscores](#highscores)
8. [Résolution des Problèmes](#résolution-des-problèmes)

---

## Introduction

Bienvenue sur la Borne d'Arcade ! Cette application permet de jouer à une collection de jeux rétro directement depuis un menu interactif. Conçue pour fonctionner sur Raspberry Pi, elle offre une expérience d'arcade authentique.

### Jeux Disponibles

La borne propose une variété de jeux :

| Jeu | Type | Description |
|-----|------|-------------|
| Pong | Java | Le classique jeu de tennis |
| Columns | Java | Jeu de puzzle type Tetris |
| Snake Eater | Java | Le serpent qui mange |
| Memory | Java | Jeu de mémoire |
| Ball Blast | Python | Détruisez les boules |
| Tron Game | Python | Course de motos |
| Piano Tile | Python | Jeu musical |
| CursedWare | Love2D | Mini-jeux variés |
| Minesweeper | - | Démineur |
| DinoRail | Java | Course de dinosaure |
| Et plus... | | |

---

## Installation

### Prérequis
- Raspberry Pi 3 ou supérieur (ou PC Linux)
- Écran 1280x1024 (recommandé)
- Clavier USB

### Installation Automatique
```bash
# Télécharger le projet
git clone <url-du-projet> borne_arcade
cd borne_arcade

# Lancer l'installation
./installation.sh
```

L'installation installe automatiquement :
- Java JDK 17
- Python 3 et pip
- Pygame et librosa
- Love2D
- Toutes les dépendances nécessaires

### Lancement Automatique au Démarrage
Pour démarrer la borne automatiquement :
```bash
mv borne.desktop ~/.config/autostart/
```

---

## Premier Lancement

### Lancer la Borne
```bash
# Mode normal (production)
./launch.sh

# ou directement
./launch-arcade.sh
```

### Ce qui se passe
1. La fenêtre de la borne s'ouvre en plein écran
2. La liste des jeux s'affiche
3. Une musique de fond se lance
4. Vous pouvez naviguer dans le menu

---

## Utilisation du Menu

### Interface Principale

```
┌─────────────────────────────────────────────────────────┐
│                                                         │
│  ┌────────────────┐    ┌───────────────────────────────┐│
│  │                │    │                               ││
│  │  Liste des     │    │   Image du jeu sélectionné   ││
│  │  jeux          │    │                               ││
│  │                │    └───────────────────────────────┘│
│  │  ► Pong        │    ┌───────────────────────────────┐│
│  │    Columns     │    │   Description du jeu          ││
│  │    Snake       │    │   Contrôles                   ││
│  │    Memory      │    │   Highscores                  ││
│  │                │    │                               ││
│  └────────────────┘    └───────────────────────────────┘│
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### Navigation

| Action | Touche (Clavier) | Touche (Borne J1) |
|--------|------------------|-------------------|
| Monter dans la liste | ↑ | Joystick Haut |
| Descendre dans la liste | ↓ | Joystick Bas |
| Lancer le jeu | R | Bouton A |
| Quitter | H | Bouton Z |

---

## Jouer

### Lancer un Jeu
1. Utilisez ↑/↓ pour sélectionner un jeu
2. Appuyez sur **R** (ou Bouton A) pour lancer
3. Le jeu démarre dans une nouvelle fenêtre

### Pendant le Jeu
- Les contrôles varient selon le jeu
- Consultez la description avant de jouer
- La plupart des jeux se quittent avec **Échap** ou un bouton spécifique

### Retour au Menu
Après avoir quitté un jeu, vous revenez automatiquement au menu principal.

---

## Contrôles

### Correspondance Clavier Standard

#### Joueur 1
```
Joystick : Flèches directionnelles (↑ ↓ ← →)

Boutons supérieurs : R  T  Y
Boutons inférieurs : F  G  H
```

#### Joueur 2
```
Joystick : O (haut)  L (bas)  K (gauche)  M (droite)

Boutons supérieurs : A  Z  E
Boutons inférieurs : Q  S  D
```

### Schéma des Contrôles dans le Menu
```
┌─────────────────────────────────────────┐
│ Bouton.txt affiche les contrôles :      │
│                                         │
│ Joystick  │ A │ B │ C │ X │ Y │ Z      │
│   ▲       │ R │ T │ Y │ F │ G │ H      │
│ ◄   ►     └───┴───┴───┴───┴───┴───┘    │
│   ▼                                     │
└─────────────────────────────────────────┘
```

---

## Highscores

### Entrer son Nom
À la fin d'un jeu (si votre score est dans le top 10) :

1. Un écran de saisie apparaît
2. Utilisez ↑/↓ pour changer la lettre
3. Utilisez →/← pour passer à la lettre suivante/précédente
4. Validez avec le bouton A

### Format du Nom
- 3 caractères maximum
- Lettres A-Z, point (.) ou espace

### Consulter les Scores
Les meilleurs scores s'affichent dans la description du jeu, sur la partie droite du menu.

---

## Résolution des Problèmes

### Le menu ne se lance pas

**Vérifiez Java :**
```bash
java -version
```
Si non installé :
```bash
sudo apt-get install openjdk-17-jdk
```

### Un jeu Python ne fonctionne pas

**Vérifiez les dépendances :**
```bash
pip3 list | grep pygame
pip3 list | grep librosa
```

**Réinstallez si nécessaire :**
```bash
pip3 install --break-system-packages pygame librosa
```

### Un jeu Love2D ne se lance pas

**Vérifiez Love2D :**
```bash
love --version
```
Si non installé :
```bash
sudo apt-get install love
```

### L'écran n'est pas en plein écran

Vérifiez la résolution de votre écran (1280x1024 recommandé).

Modifiez dans `config.properties` :
```properties
prod.fullscreen=true
```

### Pas de son

1. Vérifiez le volume système
2. Vérifiez les fichiers audio dans `src/resources/sound/bg/`

### Le jeu compile mal

Recompilez manuellement :
```bash
./installation.sh
```

Ou pour un jeu spécifique :
```bash
cd src/projets/NomDuJeu
javac -encoding UTF-8 -cp ".:../../main/java:../../../lib" *.java
```

---

## Quitter la Borne

### Quitter Proprement
1. Appuyez sur **H** (Bouton Z du joueur 1)
2. Une confirmation apparaît
3. Sélectionnez **OUI** avec **R** pour confirmer

### Arrêt Forcé
- Appuyez sur **Alt+F4** si nécessaire
- Ou **Ctrl+C** dans le terminal

---

## Conseils

- **Attendez** quelques secondes après le lancement pour que tout se charge
- **Consultez** toujours la description d'un jeu avant de jouer
- **Vérifiez** les contrôles spécifiques dans l'interface
- **Patience** : certains jeux peuvent mettre quelques secondes à se lancer

---

## Support

En cas de problème :
1. Consultez les logs dans le terminal
2. Relancez `./installation.sh` pour réparer
3. Vérifiez que tous les fichiers sont présents

---

Bon jeu ! 🎮
