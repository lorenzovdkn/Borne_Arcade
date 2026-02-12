import MG2D.geometrie.Rectangle;

/**
 * Classe abstraite représentant une boîte graphique sur l'interface.
 * 
 * Cette classe sert de base pour toutes les boîtes d'affichage de l'interface
 * de la borne d'arcade (sélection, description, image). Elle encapsule
 * un rectangle qui définit la zone d'affichage de la boîte.
 */
public abstract class Boite {
    
    /** Rectangle définissant la zone d'affichage de la boîte */
    private Rectangle rectangle;
	
    /**
     * Constructeur de la classe Boîte.
     * 
     * @param rectangle le rectangle définissant la zone d'affichage
     */
    Boite(Rectangle rectangle){
	this.rectangle = rectangle;
    }

    /**
     * Récupère le rectangle de la boîte.
     * 
     * @return le rectangle définissant la zone d'affichage
     */
    public Rectangle getRectangle() {
	return rectangle;
    }

    /**
     * Définit le rectangle de la boîte.
     * 
     * @param rectangle le nouveau rectangle à utiliser
     */
    public void setRectangle(Rectangle rectangle) {
	this.rectangle = rectangle;
    }
}
