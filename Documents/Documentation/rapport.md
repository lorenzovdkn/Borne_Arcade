# Rapport de Projet - Borne d'Arcade

## Introduction

Ce rapport présente le travail réalisé sur le projet Borne d'Arcade, une application permettant de regrouper et lancer une collection de jeux rétro depuis une interface unifiée, conçue principalement pour Raspberry Pi.

---

## Travail Réalisé

### 1. Interface Graphique et Menu Principal

- **Menu de sélection des jeux** : Interface permettant de naviguer parmi les 15 jeux disponibles (Pong, Columns, Snake Eater, Memory, Ball Blast, Tron Game, Piano Tile, CursedWare, Minesweeper, DinoRail, etc.)
- **Affichage des descriptions** : Chaque jeu possède une description et une image affichées lors de la sélection
- **Support multi-plateformes** : Fonctionne sur Raspberry Pi et PC Linux

### 2. Système de Lancement Multi-Langages

J'ai implémenté un système de détection et lancement automatique des jeux supportant plusieurs technologies :

| Type | Extension/Fichier | Commande de lancement |
|------|-------------------|----------------------|
| Shell | `*.sh` | `/bin/bash ./{script}.sh` |
| Love2D | `main.lua`, `conf.lua` | `love .` |
| Python | dossier `src/` ou `*.py` | `python3 ./src` ou `python3 main.py` |
| Java | `*.java` | `java -cp ... Main` |

Le système détecte automatiquement le type de jeu en scannant le contenu du dossier, avec un ordre de priorité défini.

### 3. Scripts d'Automatisation

#### Script d'installation (`installation.sh`)
- Mise à jour automatique du système
- Installation de toutes les dépendances (Java JDK 21, Python 3, Pygame, Love2D, librosa, etc.)
- Compilation automatique de MG2D et de tous les jeux Java
- Attribution des permissions aux scripts

#### Scripts de lancement (`launch.sh`, `launch-dev.sh`, `launch-arcade.sh`)
- Gestion de deux modes : PRODUCTION et DEVELOPPEMENT
- Compilation automatique à chaque lancement
- Lecture de la configuration depuis `config.properties`

### 4. Système de Configuration

Fichier `config.properties` permettant de configurer :
- Le mode de l'application (PRODUCTION/DEVELOPPEMENT)
- Options du mode développement (IA, documentation)
- Options du mode production (plein écran, debug)

### 5. Génération de Documentation via IA (Ollama)

J'ai développé un système de génération automatique de Javadoc utilisant le serveur de l'IUT :

- **Wrapper Python** (`ollama_wrapper.py`) : Interface complète pour communiquer avec l'API Ollama
- **Générateur de documentation** (`doc_generator.py`) : Script permettant de détecter les fonctions non documentées et de générer automatiquement la Javadoc
- **Mode interactif** : Validation manuelle des documentations générées avant application
- **Mode dry-run** : Aperçu des modifications sans les appliquer

### 7. Documentation Complète

J'ai rédigé une documentation complète comprenant :
- Guide utilisateur
- Guide d'ajout de jeu
- Documentation technique
- Documentation des automatisations

---

## Ce que j'ai voulu faire mais pas réussi

### 1. Génération de Documentation pour tous les Langages

**Objectif** : Étendre la génération automatique de documentation aux projets Python et Lua, pas seulement Java.

**Problème rencontré** : La diversité des langages utilisés (Python, Java, Lua) rend difficile l'implémentation d'un système unifié :
- Chaque langage a ses propres conventions de documentation (Javadoc, docstrings Python, LuaDoc)
- Les parseurs sont différents pour chaque langage
- Le prompt pour l'IA doit être adapté à chaque syntaxe

**État actuel** : Le système ne fonctionne que pour les fichiers Java et hors du dossier "projets".

### 2. Tests Automatisés Plus Complets

J'aurais voulu mettre en place une suite de tests plus complète pour valider automatiquement :
- La détection des types de jeux
- Le bon fonctionnement de chaque jeu
- La génération de documentation

---

## Conclusion

Le projet a atteint ses objectifs principaux : une interface fonctionnelle pour lancer des jeux de différents langages, avec des scripts d'automatisation et un système de documentation IA. Les améliorations futures pourraient inclure le support multi-langages pour la génération de documentation et une meilleure couverture de tests. 
