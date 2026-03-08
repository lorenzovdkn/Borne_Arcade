# Borne d'Arcade

Application de borne d'arcade permettant de regrouper et lancer une collection de jeux retro depuis une interface unifiee. Concue pour Raspberry Pi.

## Fonctionnalites

- Menu de selection avec navigation au clavier/joystick
- Support multi-langages : Java, Python, Lua (Love2D), Shell
- Detection automatique du type de jeu
- Systeme de highscores
- Generation automatique de Javadoc via IA (Ollama)
- Scripts d'installation et de lancement automatises

## Jeux inclus

Pong, Columns, Snake Eater, Memory, Ball Blast, Tron Game, Piano Tile, CursedWare, Minesweeper, DinoRail, JavaSpace, et plus.

## Installation

```bash
./installation.sh
```

## Lancement

```bash
./launch.sh
```

## Controles

| Action | Touche |
|--------|--------|
| Navigation | Fleches directionnelles |
| Selection | R (bouton A) |
| Quitter | H (bouton Z) |

## Structure

- `src/main/java/` : Code source principal
- `src/projets/` : Dossiers des jeux
- `src/IA/` : Generation de documentation IA
- `lib/MG2D/` : Bibliotheque graphique
- `Documents/` : Documentation complete

## Documentation

Voir le dossier `Documents/Documentation/` pour :
- Guide utilisateur
- Guide d'ajout de jeu
- Documentation technique
- Rapport