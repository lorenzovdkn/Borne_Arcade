import java.io.IOException;

import MG2D.geometrie.Texture;
import MG2D.Couleur;
import MG2D.geometrie.Point;
import MG2D.geometrie.Triangle;
import MG2D.Clavier;

/**
 * Classe gérant le pointeur de sélection de jeux dans l'interface.
 * 
 * Cette classe contrôle l'affichage et le déplacement du pointeur qui permet
 * de naviguer dans la liste des jeux disponibles. Elle gère également
 * le lancement des jeux sélectionnés.
 */
public class Pointeur {
    
    /** Index du jeu actuellement sélectionné */
    private int value;
    
    /** Texture du triangle gauche du pointeur */
    private Texture triangleGauche;
    
    /** Texture du triangle droit du pointeur */
    private Texture triangleDroite;
    
    /** Texture du rectangle central de sélection */
    private Texture rectangleCentre;

    /**
     * Constructeur du pointeur.
     * Initialise les textures du pointeur et positionne la sélection
     * sur le dernier jeu de la liste.
     */
    public Pointeur(){
	this.triangleGauche = new Texture("img/star.png", new Point(30, 492), 40,40);
	// this.triangleDroite = new Triangle(Couleur .ROUGE, new Point(550, 560), new Point(520, 510), new Point(550, 460), true);
	this.triangleDroite = new Texture("img/star.png", new Point(530, 492), 40,40);
	this.rectangleCentre = new Texture("img/select2.png", new Point(80, 460), 440, 100);
	this.value = Graphique.tableau.length-1;
    }

    /**
     * Lance le jeu actuellement sélectionné.
     * Exécute le script shell correspondant au jeu sélectionné et gère
     * la musique de fond (arrêt/reprise).
     * 
     * @param clavier le clavier de la borne pour détecter l'activation
     */
    public void lancerJeu(ClavierBorneArcade clavier){
	if(clavier.getBoutonJ1ATape()){

	    try {
		Graphique.stopMusiqueFond();
		
		String cheminJeu = Graphique.tableau[getValue()].getChemin();
		
		Process process = GameLauncher.launchGame(cheminJeu);
	
		
		process.waitFor();
		
		Graphique.lectureMusiqueFond();
	    } catch (IOException e) {
		e.printStackTrace();
	    } catch(Exception e){
		e.printStackTrace();
	    }
	}
    }

    /**
     * Récupère l'index du jeu actuellement sélectionné.
     * 
     * @return l'index du jeu sélectionné
     */
    public int getValue() {
	return value;
    }

    /**
     * Définit l'index du jeu sélectionné.
     * 
     * @param value le nouvel index de sélection
     */
    public void setValue(int value) {
	this.value = value;
    }

    /**
     * Récupère la texture du triangle gauche du pointeur.
     * @return la texture du triangle gauche
     */
    public Texture getTriangleGauche() {
	return triangleGauche;
    }

    /**
     * Définit la texture du triangle gauche du pointeur.
     * @param triangleGauche la nouvelle texture du triangle gauche
     */
    public void setTriangleGauche(Texture triangleGauche) {
	this.triangleGauche = triangleGauche;
    }

    /**
     * Récupère la texture du triangle droit du pointeur.
     * @return la texture du triangle droit
     */
    public Texture getTriangleDroite() {
	return triangleDroite;
    }

    /**
     * Définit la texture du triangle droit du pointeur.
     * @param triangleDroite la nouvelle texture du triangle droit
     */
    public void setTriangleDroite(Texture triangleDroite) {
	this.triangleDroite = triangleDroite;
    }

    /**
     * Récupère la texture du rectangle central de sélection.
     * @return la texture du rectangle central
     */
    public Texture getRectangleCentre() {
	return rectangleCentre;
    }

    /**
     * Définit la texture du rectangle central de sélection.
     * @param rectangleCentre la nouvelle texture du rectangle central
     */
    public void setRectangleCentre(Texture rectangleCentre) {
	this.rectangleCentre = rectangleCentre;
    }

}
