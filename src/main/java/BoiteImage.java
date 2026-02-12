import MG2D.geometrie.Point;
import MG2D.geometrie.Rectangle;
import MG2D.geometrie.Texture;
import java.io.File;

/**
 * Classe gérant l'affichage des images des jeux dans l'interface.
 * 
 * Cette classe hérite de Boite et gère l'affichage de l'image
 * de prévisualisation du jeu actuellement sélectionné.
 */
public class BoiteImage extends Boite{

    /** Texture de l'image à afficher */
    Texture image;

    /**
     * Constructeur de la boîte d'image.
     * 
     * @param rectangle zone d'affichage de l'image
     * @param image chemin vers le répertoire contenant l'image du jeu
     */
    BoiteImage(Rectangle rectangle, String image) {
	super(rectangle);
	String imagePath = getImagePath(image);
	this.image = new Texture(imagePath, new Point(760, 648));
    }

    /**
     * Construit le chemin vers l'image de prévisualisation d'un jeu.
     * 
     * @param projectPath chemin vers le répertoire du projet (ex: "src/projets/Columns")
     * @return le chemin vers le fichier photo_small.png du projet
     */
    private String getImagePath(String projectPath) {
        if (projectPath.startsWith("src/projets/")) {
            projectPath = projectPath.substring("src/projets/".length());
        }
        // Chaque projet contient un fichier photo_small.png à sa racine
        return projectPath + "/photo_small.png";
    }

    /**
     * Récupère la texture de l'image.
     * 
     * @return la texture de l'image actuellement affichée
     */
    public Texture getImage() {
	return this.image;
    }

    /**
     * Change l'image affichée.
     * 
     * @param chemin chemin vers le répertoire contenant la nouvelle image
     */
    public void setImage(String chemin) {
        if (chemin.startsWith("src/projets/")) {
            chemin = chemin.substring("src/projets/".length());
        }
	this.image.setImg(chemin+"/photo_small.png");
	//this.image.setTaille(400, 320);
    }

}
