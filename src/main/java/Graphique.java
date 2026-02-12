import java.awt.Font;
import java.io.IOException;
import java.nio.file.*;
import javax.swing.*;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;

import MG2D.geometrie.*;
import MG2D.geometrie.Point;
import MG2D.audio.*;
import MG2D.*;
import MG2D.FenetrePleinEcran;

/**
 * Classe principale de gestion de l'interface graphique de la borne d'arcade.
 * 
 * Cette classe gère l'affichage de l'écran de sélection des jeux, l'interface utilisateur,
 * la musique de fond et les interactions avec le clavier de la borne.
 * Elle coordonne les différents éléments visuels : liste des jeux, descriptions, 
 * images et contrôles de navigation.
 */
public class Graphique {

    /** Fenêtre principale en plein écran */
    private static final FenetrePleinEcran f = new FenetrePleinEcran("_Menu Borne D'arcade_");
    
    /** Largeur de l'écran */
    private int TAILLEX;
    
    /** Hauteur de l'écran */
    private int TAILLEY;
    
    /** Gestionnaire du clavier de la borne */
    private ClavierBorneArcade clavier;
    
    /** Boîte de sélection des jeux */
    private BoiteSelection bs;
    
    /** Boîte d'affichage des images */
    private BoiteImage bi;
    
    /** Boîte d'affichage des descriptions */
    private BoiteDescription bd;
    
    /** Tableau des boutons de jeux disponibles */
    public static Bouton[] tableau;
    
    /** Pointeur de sélection */
    private Pointeur pointeur;
    
    /** Police de caractères principale */
    Font font;
    
    /** Police de caractères pour la sélection */
    Font fontSelect;
    
    /** État d'affichage des textes */
    public static boolean[] textesAffiches;
    
    /** Musique de fond en cours */
    public static Bruitage musiqueFond;
    
    /** Liste des musiques de fond disponibles */
    private static String[] tableauMusiques;
    
    /** Compteur pour la sélection des musiques */
    private static int cptMus;

    /**
     * Constructeur de la classe Graphique.
     * Initialise l'interface graphique de la borne d'arcade, charge les jeux disponibles,
     * configure l'affichage et démarre la musique de fond.
     */
    public Graphique(){
    	

	TAILLEX = 1280;
	TAILLEY = 1024;

	font = null;
	try{
	    File in = new File("src/resources/fonts/PrStart.ttf");
	    font = font.createFont(Font.TRUETYPE_FONT, in);
	    font = font.deriveFont(32.0f);
	}catch (Exception e) {
	    System.err.println(e.getMessage());
	}

	//f = new Fenetre("_Menu Borne D'arcade_",TAILLEX,TAILLEY);
	f.setVisible(true);
	f.setFocusable(true);
	f.requestFocus();
	clavier = new ClavierBorneArcade();
	f.addKeyListener(clavier);
	f.getP().addKeyListener(clavier);

	/*Retrouver le nombre de jeux dispo*/
	Path yourPath = FileSystems.getDefault().getPath("src/projets/");
	int cpt=0;
	try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(yourPath)) {
	    for (Path path : directoryStream) {
		cpt++;
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}

	tableau = new Bouton[cpt];
	textesAffiches = new boolean[cpt];
	for(int i=0;i<cpt;i++){
		textesAffiches[i]=true;
	}
	
	Bouton.remplirBouton();
	pointeur = new Pointeur();
	bs = new BoiteSelection(new Rectangle(Couleur .GRIS_CLAIR, new Point(0, 0), new Point(640, TAILLEY), true), pointeur);
	//f.ajouter(bs.getRectangle());
	//System.out.println(tableau[pointeur.getValue()].getChemin());
	bi = new BoiteImage(new Rectangle(Couleur .GRIS_FONCE, new Point(640, 512), new Point(TAILLEX, TAILLEY), true), new String(tableau[pointeur.getValue()].getChemin()));
	//f.ajouter(bi.getRectangle());
	bd = new BoiteDescription(new Rectangle(Couleur .GRIS, new Point(640, 0), new Point(TAILLEX, 512), true));
	bd.lireFichier(tableau[pointeur.getValue()].getChemin());
	bd.lireHighScore(tableau[pointeur.getValue()].getChemin());
	//f.ajouter(bd.getRectangle());

	Texture fond = new Texture("img/fondretro3.png", new Point(0, 0), TAILLEX, TAILLEY);
	f.ajouter(fond);
	//ajout apres fond car bug graphique sinon
	f.ajouter(bi.getImage());
	for(int i = 0 ; i < bd.getMessage().length ; i++){
	    f.ajouter(bd.getMessage()[i]);
	}
	//f.ajouter(bd.getMessage());
	f.ajouter(pointeur.getTriangleGauche());
	f.ajouter(pointeur.getTriangleDroite());
	for(int i = 0 ; i < tableau.length ; i++){
	    f.ajouter(tableau[i].getTexture());
	}
	f.ajouter(pointeur.getRectangleCentre());
	for(int i = 0 ; i < tableau.length ; i++){
	    f.ajouter(tableau[i].getTexte());
	    tableau[i].getTexte().setPolice(font);
	    tableau[i].getTexte().setCouleur(Couleur.BLANC);
	}
	//add texture
	for(int i = 0 ; i < bd.getBouton().length ; i++){
	    f.ajouter(bd.getBouton()[i]);
	}
	f.ajouter(bd.getJoystick());
	//add texte
	for(int i = 0 ; i < bd.gettBouton().length ; i++){
	    f.ajouter(bd.gettBouton()[i]);
	}
	f.ajouter(bd.gettJoystick());
	f.ajouter(new Ligne(Couleur.NOIR,new Point(670,360), new Point(1250,360)));
	f.ajouter(new Ligne(Couleur.NOIR,new Point(670,190), new Point(1250,190)));
	f.ajouter(new Ligne(Couleur.NOIR,new Point(960,210), new Point(960,310)));
	f.ajouter(bd.getHighscore());
	for(int i = 0 ; i < bd.getListeHighScore().length ; i++){
	    f.ajouter(bd.getListeHighScore()[i]);
	}
	
	/*Musique de fond*/
	//Comptage du nombre de musiques disponibles
	Path cheminMusiques = FileSystems.getDefault().getPath("src/resources/sound/bg/");
	cptMus=0;
	try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(cheminMusiques)) {
	    for (Path path : directoryStream) {
		cptMus++;
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
	//Creation d'un tableau de musiques
	tableauMusiques = new String[cptMus];
	try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(cheminMusiques)) {
	    int i = cptMus-1;
	    for (Path path : directoryStream) {
		tableauMusiques[i]=path.getFileName().toString();
		i--;
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
	//Choix d'une musique aleatoire et lecture de celle-ci
	this.lectureMusiqueFond();
    }

	/**
	 * Boucle principale de sélection des jeux.
	 * Affiche l'écran de sélection des jeux, gère les interactions avec le clavier
	 */
    public void selectionJeu(){	
		Texture fondBlancTransparent = new Texture("./img/blancTransparent.png", new Point(0,0));
		Rectangle boutonNon = new Rectangle(Couleur.ROUGE, new Point(340, 600), 200, 100, true);
		Rectangle boutonOui = new Rectangle(Couleur.VERT, new Point(740, 600), 200, 100, true);
		Texte message = new Texte(Couleur.NOIR, "Voulez vous vraiment quitter ?", font, new Point(640, 800));
		Texte non = new Texte(Couleur.NOIR, "NON", font, new Point(440, 650));
		Texte oui = new Texte(Couleur.NOIR, "OUI", font, new Point(840, 650));
		Rectangle rectSelection = new Rectangle(Couleur.BLEU, new Point(330,590),220,120, true);
		int frame=0;
		boolean fermetureMenu=false;
		int selectionSur = 0;
		Texte textePrec=tableau[pointeur.getValue()].getTexte();
		while(true){
			try {
				if(frame==0){
					if(textesAffiches[pointeur.getValue()]==true){
						f.supprimer(tableau[pointeur.getValue()].getTexte());
						textesAffiches[pointeur.getValue()]=false;
					}
				}
				if(frame==3){
					if(textesAffiches[pointeur.getValue()]==false){
						f.ajouter(tableau[pointeur.getValue()].getTexte());
						textesAffiches[pointeur.getValue()]=true;
					}
				}
				if(frame==6){
					frame=-1;
				}
				frame++;
				// System.out.println("frame n°"+frame);
			}
			catch (Exception e) {
				System.err.println(e.getMessage());
			}
			try{
				Thread.sleep(50);
			}catch(Exception e){}
			
			if(!fermetureMenu){
				if(bs.selection(clavier)){
				bi.setImage(tableau[pointeur.getValue()].getChemin());

				fontSelect = null;
				try{
				File in = new File("src/resources/fonts/PrStart.ttf");
				fontSelect = fontSelect.createFont(Font.TRUETYPE_FONT, in);
				fontSelect = fontSelect.deriveFont(48.0f);
				}catch (Exception e) {
				System.err.println(e.getMessage());
				}

				// if(!tableau[pointeur.getValue()].getTexte().getPolice().equals(fontSelect)){
				// tableau[pointeur.getValue()].getTexte().setPolice(fontSelect);
				// }
				
				
				
				

				tableau[pointeur.getValue()].getTexte().setPolice(font);

				bd.lireFichier(tableau[pointeur.getValue()].getChemin());
				bd.lireHighScore(tableau[pointeur.getValue()].getChemin());
				bd.lireBouton(tableau[pointeur.getValue()].getChemin());
				// System.out.println(tableau[pointeur.getValue()].getChemin());
				//bd.setMessage(tableau[pointeur.getValue()].getNom());
				pointeur.lancerJeu(clavier);
				
				
				}else{
					f.ajouter(fondBlancTransparent);
					f.ajouter(message);
					f.ajouter(rectSelection);
					f.ajouter(boutonNon);
					f.ajouter(boutonOui);
					f.ajouter(non);
					f.ajouter(oui);
					fermetureMenu=true;
					
				}
			}else{
					if(clavier.getJoyJ1DroiteEnfoncee()){
						selectionSur=1;
					}
						
					if(clavier.getJoyJ1GaucheEnfoncee()){
						selectionSur=0;
					}
					   
					
					if(selectionSur==0){
						rectSelection.setA(new Point(330,590));
						rectSelection.setB(new Point(550,710));
					}
					else{
						rectSelection.setB(new Point(950,710));
						rectSelection.setA(new Point(730,590));
						
					}
					if(clavier.getBoutonJ1ATape()){
						if(selectionSur==0){
							f.supprimer(fondBlancTransparent);
							f.supprimer(message);
							f.supprimer(rectSelection);
							f.supprimer(boutonNon);
							f.supprimer(boutonOui);
							f.supprimer(non);
							f.supprimer(oui);
							fermetureMenu=false;
						}
						else{
							System.exit(0);
						}
					}

			}
			f.rafraichir();
		}//fin while true
    }
    
	/**
	 * Lance la musique de fond de manière aléatoire parmi les musiques disponibles.
	 * Sélectionne une musique aléatoire depuis le tableau des musiques et la joue en boucle.
	 */
    public static void lectureMusiqueFond() {
    	musiqueFond = new Bruitage ("sound/bg/"+tableauMusiques[(int)(Math.random()*cptMus)]);
    	musiqueFond.lecture();
    }
	
	/**
	 * Arrête la musique de fond actuellement en cours de lecture.
	 * Permet d'arrêter la musique de fond avant de lancer un jeu et de la reprendre après la fin du jeu.
	 */
	public static void stopMusiqueFond(){
		musiqueFond.arret();
	}
	
	/**
	 * Affiche le texte de description du jeu sélectionné.
	 * Récupère le texte de description du jeu sélectionné et l'affiche dans la boîte de description.
	 * Cette méthode est appelée lors de la sélection d'un jeu pour mettre à jour la description
	 * @param valeur l'index du jeu sélectionné dans le tableau de boutons
	 */
	public static void afficherTexte(int valeur){
		f.ajouter(tableau[valeur].getTexte());
	}
}
