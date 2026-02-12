import MG2D.*;
import MG2D.geometrie.*;

/**
 * Classe de test pour la fonctionnalité de gestion des meilleurs scores.
 * 
 * Cette classe sert à tester l'affichage et l'enregistrement des high scores,
 * notamment l'interface de saisie du nom du joueur après un score élevé.
 */
class TestHighScore{

    /**
     * Méthode principale de test pour les high scores.
     * 
     * Crée une fenêtre de test et lance l'interface de saisie du nom
     * pour enregistrer un score de 40000 points.
     * 
     * @param args arguments de ligne de commande (non utilisés)
     */
    public static void main(String[] args){
	Fenetre f = new Fenetre("test",1280,1024);
	ClavierBorneArcade clavier = new ClavierBorneArcade();
	f.addKeyListener(clavier);

	HighScore.demanderEnregistrerNom(f,clavier,new Texture("img/shoot.png",new Point(0,0)),40000,"./fichierTestHighScore/text5.hig");
    }
    
}
