import java.awt.Font;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;


import MG2D.Couleur;
import MG2D.geometrie.Point;
import MG2D.geometrie.Texture;
import MG2D.geometrie.Texte;

/**
 * Classe représentant un bouton de jeu dans l'interface de la borne d'arcade.
 * 
 * Chaque bouton correspond à un jeu disponible et contient les informations
 * nécessaires pour l'afficher (texte, texture) et l'exécuter (chemin, nom).
 * Cette classe gère également le chargement automatique de tous les jeux
 * disponibles depuis le répertoire "projet/".
 */
public class Bouton {
    
    /** Texte affiché pour le bouton */
    private Texte texte;
    
    /** Chemin vers le répertoire du jeu */
    private String chemin;
    
    /** Nom du jeu */
    private String nom;
    
    /** Texture d'arrière-plan du bouton */
    private Texture texture;
    
    /** Numéro d'identification du jeu */
    private int numeroDeJeu;

    /**
     * Constructeur par défaut.
     * Initialise un bouton vide.
     */
    public Bouton(){
	this.texte = null;
	this.texture = null;
	this.chemin = null;
	this.nom = null;
    }

    /**
     * Constructeur complet.
     * 
     * @param texte le texte à afficher sur le bouton
     * @param texture la texture d'arrière-plan du bouton
     * @param chemin le chemin vers le répertoire du jeu
     * @param nom le nom du jeu
     */
    public Bouton(Texte texte, Texture texture, String chemin, String nom){
	this.texte = texte;
	this.texture = texture;
	this.chemin = chemin;
	this.nom = nom;
    }

    /**
     * Remplit automatiquement le tableau de boutons avec tous les jeux disponibles.
     * Parcourt le répertoire "projet/" et crée un bouton pour chaque jeu trouvé.
     * Positionne les boutons verticalement dans l'interface.
     */
    public static void remplirBouton(){
	for(int i = 0 ; i < Graphique.tableau.length ; i++){
	    Graphique.tableau[i] = new Bouton();
	}

	Path yourPath = FileSystems.getDefault().getPath("src/projets/");

	try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(yourPath)) {
	    int i = Graphique.tableau.length - 1;
	    for (Path path : directoryStream) {
		Graphique.tableau[i].setTexte(new Texte(Couleur .NOIR, path.getFileName().toString(), new Font("Calibri", Font.TYPE1_FONT, 30), new Point(310, 510)));
		Graphique.tableau[i].setTexture(new Texture("img/bouton2.png", new Point(100, 478), 400, 65));
		for(int j=0;j<Graphique.tableau.length-(i+1);j++){
		    Graphique.tableau[i].getTexte().translater(0,-110);
		    Graphique.tableau[i].getTexture().translater(0,-110);
		}
		Graphique.tableau[i].setChemin("src/projets/"+path.getFileName().toString());
		Graphique.tableau[i].setNom(path.getFileName().toString());
		Graphique.tableau[i].setNumeroDeJeu(i);
		i--;
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}

    }

    /**
     * Récupère le chemin vers le répertoire du jeu associé à ce bouton.
     * @return le chemin vers le répertoire du jeu
     */
    public String getChemin() {
	return chemin;
    }

    /**
     * Définit le chemin vers le répertoire du jeu associé à ce bouton.
     * @param chemin le chemin vers le répertoire du jeu
     */
    public void setChemin(String chemin) {
	this.chemin = chemin;
    }

    /**
     * Récupère le nom du jeu associé à ce bouton.
     * @return le nom du jeu
     */
    public String getNom() {
	return nom;
    }

    /**
     * Définit le nom du jeu associé à ce bouton.
     * @param nom le nom du jeu
     */
    public void setNom(String nom) {
	this.nom = nom;
    }

    /**
     * Récupère le texte affiché pour ce bouton.
     * @return le texte affiché
     */
    public Texte getTexte() {
	return texte;
    }

    /**
     * Définit le texte affiché pour ce bouton.
     * @param texte le texte à afficher
     */
    public void setTexte(Texte texte) {
	this.texte = texte;
    }

    /**
     * Récupère la texture d'arrière-plan du bouton.
     * @return la texture du bouton
     */
    public Texture getTexture() {
	return texture;
    }

    /**
     * Définit la texture d'arrière-plan du bouton.
     * @param texture la texture à définir
     */
    public void setTexture(Texture texture) {
	this.texture = texture;
    }

    /**
     * Récupère le numéro d'identification du jeu associé à ce bouton.
     * @return le numéro d'identification du jeu
     *  
     * Ce numéro est utilisé pour identifier de manière unique chaque jeu dans le tableau de boutons.
     */
    public int getNumeroDeJeu() {
	return numeroDeJeu;
    }

    /**
     * Définit le numéro d'identification du jeu associé à ce bouton.
     * @param numeroDeJeu le numéro d'identification du jeu
     *  
     * Ce numéro est utilisé pour identifier de manière unique chaque jeu dans le tableau de boutons.
     */
    public void setNumeroDeJeu(int numeroDeJeu) {
	this.numeroDeJeu = numeroDeJeu;
    }
}
