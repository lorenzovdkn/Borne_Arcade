# Guide d'Ajout d'un Nouveau Jeu

Ce guide explique comment ajouter un nouveau jeu à la borne d'arcade.

## Table des Matières

1. [Vue d'Ensemble](#vue-densemble)
2. [Structure Requise](#structure-requise)
3. [Types de Jeux Supportés](#types-de-jeux-supportés)
4. [Guide Pas à Pas](#guide-pas-à-pas)
5. [Fichiers de Configuration](#fichiers-de-configuration)
6. [Exemples Complets](#exemples-complets)
7. [Intégration des Contrôles](#intégration-des-contrôles)
8. [Système de Highscores](#système-de-highscores)
9. [Bonnes Pratiques](#bonnes-pratiques)

---

## Vue d'Ensemble

Pour ajouter un jeu à la borne, vous devez :
1. Créer un dossier dans `src/projets/`
2. Y placer votre code de jeu
3. Créer les fichiers de métadonnées obligatoires
4. (Optionnel) Ajouter une image et un fichier highscore

**La borne détecte automatiquement les nouveaux jeux** au démarrage.

---

## Structure Requise

### Structure Minimale (Obligatoire)
```
src/projets/MonJeu/
├── bouton.txt           # Description des contrôles (OBLIGATOIRE)
├── description.txt      # Description du jeu (OBLIGATOIRE)
└── [fichiers du jeu]    # Code source
```

### Structure Complète (Recommandée)
```
src/projets/MonJeu/
├── bouton.txt           # Description des contrôles
├── description.txt      # Description du jeu
├── highscore            # Fichier des scores
├── photo.png            # Image grande (640x512)
├── photo_small.png      # Image menu (400x65)
├── Main.java            # Point d'entrée (Java)
├── main.py              # Point d'entrée (Python)
├── main.lua             # Point d'entrée (Love2D)
├── monjeu.sh            # Script de lancement (Shell)
├── requirements.txt     # Dépendances Python (si applicable)
├── src/                 # Code source organisé
├── assets/              # Ressources (images, sons)
└── README.md            # Documentation du jeu
```

---

## Types de Jeux Supportés

### Priorité de Détection
Le système détecte le type dans cet ordre :

| Priorité | Type | Fichier(s) Détecté(s) |
|----------|------|----------------------|
| 1 | Shell | `*.sh` |
| 2 | Love2D | `main.lua` ou `conf.lua` |
| 3 | Python | dossier `src/` ou `*.py` |
| 4 | Java | `*.java` ou `*.class` |

### 1. Jeu Java

```
MonJeuJava/
├── bouton.txt
├── description.txt
├── Main.java            # Point d'entrée obligatoire
├── AutresClasses.java
├── ClavierBorneArcade.java  # Copier depuis un autre jeu
└── assets/
```

**Commande de lancement automatique :**
```bash
java -cp .:../../main/java:../../../lib:../../../src/resources Main
```

### 2. Jeu Python

```
MonJeuPython/
├── bouton.txt
├── description.txt
├── requirements.txt     # Dépendances pip
└── src/
    ├── __main__.py      # Point d'entrée
    └── game.py
```

**Commande de lancement automatique :**
```bash
python3 ./src
```

### 3. Jeu Love2D (Lua)

```
MonJeuLove/
├── bouton.txt
├── description.txt
├── main.lua             # Point d'entrée Love2D
├── conf.lua             # Configuration Love2D
└── assets/
```

**Commande de lancement automatique :**
```bash
love .
```

### 4. Jeu avec Script Shell

```
MonJeuShell/
├── bouton.txt
├── description.txt
├── monjeu.sh            # Script de lancement personnalisé
└── [autres fichiers]
```

**Commande de lancement automatique :**
```bash
/bin/bash ./monjeu.sh
```

---

## Guide Pas à Pas

### Étape 1 : Créer le Dossier
```bash
mkdir src/projets/MonNouveauJeu
cd src/projets/MonNouveauJeu
```

### Étape 2 : Créer `description.txt`
```
Mon Nouveau Jeu par Votre Nom - 2025

Une description courte de votre jeu qui sera
affichée dans le menu de la borne d'arcade.
Gardez-la concise (3-5 lignes).
```

### Étape 3 : Créer `bouton.txt`
Format : `Joystick:A:B:C:X:Y:Z`
```
Déplacer:Tirer:Saut:rien:Menu:Pause:Quitter
```

Chaque position correspond à un contrôle :
- Position 1 : Action du Joystick
- Position 2-7 : Boutons A, B, C, X, Y, Z

Utilisez `rien` ou `aucun` pour les boutons non utilisés.

### Étape 4 : Ajouter le Code du Jeu
Placez votre code selon le type choisi.

### Étape 5 : (Optionnel) Ajouter les Images
```
photo.png       # 640x512 pixels, affichée en grand
photo_small.png # 400x65 pixels, miniature du menu
```

### Étape 6 : (Optionnel) Créer le Fichier Highscore
```bash
touch highscore
```
Le fichier sera rempli automatiquement par le jeu.

### Étape 7 : Tester
Relancez la borne :
```bash
./launch.sh
```
Votre jeu devrait apparaître dans la liste !

---

## Fichiers de Configuration

### bouton.txt - Format Détaillé

```
[Action Joystick]:[Bouton A]:[Bouton B]:[Bouton C]:[Bouton X]:[Bouton Y]:[Bouton Z]
```

**Exemples :**
```
# Jeu de tir
Déplacer:Tirer:rien:rien:rien:rien:Quitter

# Jeu de plateforme
Déplacer:Sauter:Courir:rien:rien:Pause:Menu

# Jeu de puzzle
Sélection:Valider:Annuler:rien:Aide:rien:Quitter

# Jeu 2 joueurs
J1 Déplace:J1 Action:J2 Action:rien:rien:rien:Quitter
```

### description.txt - Format

```
Titre du Jeu par Auteur - Année

Description ligne 1
Description ligne 2
Description ligne 3
...
```

**Recommandations :**
- Première ligne : Titre, auteur, année
- Lignes suivantes : Description courte (3-5 lignes)
- Pas de formatage spécial

### highscore - Format

```
NOM-SCORE
NOM-SCORE
NOM-SCORE
```

**Exemple :**
```
ABC-1500
DEF-1200
GHI-800
```

- NOM : 3 caractères (A-Z, espace, point)
- SCORE : Nombre entier
- Maximum 10 entrées (les plus hautes en premier)

---

## Exemples Complets

### Exemple 1 : Jeu Java Simple

```java
// Main.java
import MG2D.*;
import MG2D.geometrie.*;

public class Main {
    public static void main(String[] args) {
        Fenetre f = new Fenetre("Mon Jeu", 800, 600);
        ClavierBorneArcade clavier = new ClavierBorneArcade();
        f.addKeyListener(clavier);
        
        // Boucle de jeu
        while (!clavier.getZ1()) {  // Z1 pour quitter
            // Logique du jeu
            if (clavier.getHaut1()) {
                // Joueur monte
            }
            try { Thread.sleep(16); } catch (Exception e) {}
        }
        
        System.exit(0);
    }
}
```

**ClavierBorneArcade.java** - Copiez-le depuis un jeu existant :
```bash
cp src/projets/Pong/ClavierBorneArcade.java src/projets/MonJeu/
```

### Exemple 2 : Jeu Python avec Pygame

```python
# src/__main__.py
import pygame
import sys

def main():
    pygame.init()
    screen = pygame.display.set_mode((800, 600))
    pygame.display.set_caption("Mon Jeu Python")
    clock = pygame.time.Clock()
    
    running = True
    while running:
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                running = False
            if event.type == pygame.KEYDOWN:
                if event.key == pygame.K_h:  # Bouton Z (quitter)
                    running = False
        
        keys = pygame.key.get_pressed()
        if keys[pygame.K_UP]:    # Joystick haut
            pass
        if keys[pygame.K_r]:     # Bouton A
            pass
        
        screen.fill((0, 0, 0))
        pygame.display.flip()
        clock.tick(60)
    
    pygame.quit()
    sys.exit()

if __name__ == "__main__":
    main()
```

**requirements.txt :**
```
pygame>=2.0.0
```

### Exemple 3 : Jeu Love2D

```lua
-- main.lua
function love.load()
    x, y = 400, 300
    speed = 200
end

function love.update(dt)
    -- Contrôles borne (Joueur 1)
    if love.keyboard.isDown("up") then y = y - speed * dt end
    if love.keyboard.isDown("down") then y = y + speed * dt end
    if love.keyboard.isDown("left") then x = x - speed * dt end
    if love.keyboard.isDown("right") then x = x + speed * dt end
    
    -- Quitter avec H (Bouton Z)
    if love.keyboard.isDown("h") then
        love.event.quit()
    end
end

function love.draw()
    love.graphics.circle("fill", x, y, 20)
end
```

```lua
-- conf.lua
function love.conf(t)
    t.window.title = "Mon Jeu Love2D"
    t.window.width = 800
    t.window.height = 600
end
```

---

## Intégration des Contrôles

### Mapping Clavier Standard

| Joueur | Contrôle | Touche |
|--------|----------|--------|
| J1 | Haut | ↑ (UP) |
| J1 | Bas | ↓ (DOWN) |
| J1 | Gauche | ← (LEFT) |
| J1 | Droite | → (RIGHT) |
| J1 | Bouton A | R |
| J1 | Bouton B | T |
| J1 | Bouton C | Y |
| J1 | Bouton X | F |
| J1 | Bouton Y | G |
| J1 | Bouton Z | H |
| J2 | Haut | O |
| J2 | Bas | L |
| J2 | Gauche | K |
| J2 | Droite | M |
| J2 | Bouton A | A |
| J2 | Bouton B | Z |
| J2 | Bouton C | E |
| J2 | Bouton X | Q |
| J2 | Bouton Y | S |
| J2 | Bouton Z | D |

### Utiliser ClavierBorneArcade (Java)

```java
ClavierBorneArcade clavier = new ClavierBorneArcade();
fenetre.addKeyListener(clavier);

// Dans la boucle de jeu
if (clavier.getHaut1()) { /* Joueur 1 monte */ }
if (clavier.getBas1())  { /* Joueur 1 descend */ }
if (clavier.getA1())    { /* Bouton A joueur 1 */ }
if (clavier.getZ1())    { /* Quitter (bouton Z) */ }

// Joueur 2
if (clavier.getHaut2()) { /* Joueur 2 monte */ }
if (clavier.getA2())    { /* Bouton A joueur 2 */ }
```

---

## Système de Highscores

### Intégrer les Highscores dans votre Jeu Java

```java
// À la fin du jeu
int scoreJoueur = 1500;
String fichierHighScore = "highscore";

HighScore.demanderEnregistrerNom(
    fenetre,
    clavier,
    textureBackground,
    scoreJoueur,
    fichierHighScore
);
```

### Gérer Manuellement (Python/Lua)

```python
# Python - Lire les scores
def lire_highscores():
    scores = []
    try:
        with open('highscore', 'r') as f:
            for line in f:
                if '-' in line:
                    nom, score = line.strip().split('-')
                    scores.append((nom, int(score)))
    except FileNotFoundError:
        pass
    return sorted(scores, key=lambda x: x[1], reverse=True)[:10]

# Python - Sauvegarder
def sauvegarder_highscore(nom, score):
    scores = lire_highscores()
    scores.append((nom[:3].upper(), score))
    scores = sorted(scores, key=lambda x: x[1], reverse=True)[:10]
    
    with open('highscore', 'w') as f:
        for n, s in scores:
            f.write(f"{n}-{s}\n")
```

---

## Bonnes Pratiques

### Recommandations Générales

1. **Résolution** : Concevez pour 1280x1024 ou plus petit
2. **Plein écran** : Utilisez le plein écran si possible
3. **Quitter** : Toujours avoir un moyen de quitter (bouton Z recommandé)
4. **Fenêtre** : Le jeu doit se fermer proprement (pas de processus orphelin)

### Structure de Code

```
MonJeu/
├── bouton.txt
├── description.txt
├── highscore
├── photo.png
├── photo_small.png
├── Main.java (ou équivalent)
├── src/              # Votre code
│   ├── game/        # Logique de jeu
│   ├── ui/          # Interface
│   └── utils/       # Utilitaires
├── assets/          # Ressources
│   ├── images/
│   ├── sounds/
│   └── fonts/
└── README.md        # Documentation
```

### Checklist Avant Publication

- [ ] `bouton.txt` créé avec les bons contrôles
- [ ] `description.txt` avec titre, auteur et description
- [ ] Le jeu se lance correctement
- [ ] Le jeu se ferme proprement avec le bouton Z (H)
- [ ] Les contrôles fonctionnent
- [ ] (Optionnel) Images ajoutées
- [ ] (Optionnel) Highscores fonctionnels
- [ ] (Optionnel) README.md pour les développeurs

---

## Dépannage

### Le jeu n'apparaît pas dans le menu
- Vérifiez que le dossier est dans `src/projets/`
- Vérifiez que `bouton.txt` et `description.txt` existent
- Relancez la borne

### Erreur de compilation Java
```bash
cd src/projets/MonJeu
javac -encoding UTF-8 -cp ".:../../main/java:../../../lib" *.java
```

### Le jeu se lance mais ne répond pas
- Vérifiez les contrôles utilisés
- Assurez-vous d'avoir ajouté le KeyListener (Java)
- Testez avec les touches du clavier standard

### Les dépendances Python manquent
Ajoutez-les à `requirements.txt` et exécutez :
```bash
pip3 install --break-system-packages -r requirements.txt
```

Ou modifiez `installation.sh` pour ajouter l'installation automatique.

---

## Ressources

- **MG2D Documentation** : Voir `lib/MG2D/`
- **Jeux existants** : Inspirez-vous des jeux dans `src/projets/`
- **Polices** : Disponibles dans `src/resources/fonts/`
- **Sons/Images** : `src/resources/sound/` et `src/resources/img/`

---

Bon développement ! 🎮
