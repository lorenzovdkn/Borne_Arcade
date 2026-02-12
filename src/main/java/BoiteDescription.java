import java.awt.Font;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.File;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.InputStreamReader;
import MG2D.geometrie.Texture;	
import MG2D.Couleur;
import MG2D.geometrie.Point;
import MG2D.geometrie.Rectangle;
import MG2D.geometrie.Texte;
import java.io.IOException;

/**
 * Classe gérant l'affichage des descriptions des jeux dans l'interface.
 * 
 * Cette classe hérite de Boite et gère l'affichage de la description
 * du jeu sélectionné, des contrôles nécessaires et du tableau des scores.
 * Elle lit les informations depuis des fichiers de configuration.
 */
public class BoiteDescription extends Boite{

    /** Tableau des lignes de texte de la description */
    private Texte[] message;
    
    /** Indicateur d'arrêt pour l'affichage */
    private boolean stop;
    
    /** Nombre de lignes dans la description */
    private int nombreLigne;
    
    /** Texture du joystick pour afficher les contrôles */
    private Texture joystick;
    
    /** Tableau des textures des boutons */
    private Texture[] bouton;
    
    /** Texte descriptif du joystick */
    private Texte tJoystick;
    
    /** Textes descriptifs des boutons */
    private Texte[] tBouton;
    
    /** Tableau des descriptions textuelles des boutons */
    private String[] texteBouton;
    
    /** Texte du titre "High Score" */
    private Texte highscore;
    
    /** Liste des meilleurs scores à afficher */
    private Texte[] listeHighScore;
	
	/*HACKED BY BENDAL*/
	/** Police pour les titres */
	private Font font1 = null;
	
	/** Police pour le texte normal */
	private Font font2 = null;
	
	/** Police pour les contrôles */
	private Font font3 = null;
	
	/** Police pour les scores */
	private Font font4 = null;
	/****************/
	
	
    /**
     * Constructeur de la boîte de description.
     * 
     * Initialise toutes les textures, les polices et les textes nécessaires
     * pour afficher la description du jeu, les contrôles et les scores.
     * 
     * @param rectangle zone d'affichage de la boîte de description
     */
    BoiteDescription(Rectangle rectangle) {
	super(rectangle);
	
	/*HACKED BY BENDAL*/
	try{
	    
	    Font font = null;
		Font fontTexte = null;
		File in = new File("src/resources/fonts/PrStart.ttf");
		font = font.createFont(Font.TRUETYPE_FONT, in);
		 in = new File("src/resources/fonts/Volter__28Goldfish_29.ttf");
		fontTexte = fontTexte.createFont(Font.TRUETYPE_FONT, in);
	    font1 = fontTexte.deriveFont(15.0f);
		font2 = fontTexte.deriveFont(20.0f);
		font3 = font.deriveFont(25.0f);
		font4 = font.deriveFont(14.0f);
	}catch (Exception e) {
	    System.err.println(e.getMessage());
	}
	/****************/
	
	bouton = new Texture[6];
	tBouton = new Texte[6];
	texteBouton = new String[7];
		
	//declaration des texture bouton + joystick
	this.joystick = new Texture("img/joystick2.png", new Point(740, 100), 40,40);
	for(int i = 0 ; i < 3 ; i++){
	    this.bouton[i] = new Texture("img/ibouton2.png", new Point(890+130*i, 130), 40, 40);
	}
	for(int i = 3 ; i < 6 ; i++){
	    this.bouton[i] = new Texture("img/ibouton2.png", new Point(890+130*(i-3), 50), 40, 40);
	}
	
	//declaration des textes bouton + joystick
	this.tJoystick = new Texte(Couleur .NOIR, "...", font1, new Point(760, 80));
	for(int i = 0 ; i < 3 ; i++){
	    this.tBouton[i] = new Texte(Couleur .NOIR, "...", font1, new Point(910+130*i, 120));
	}
	for(int i = 3 ; i < 6 ; i++){
	    this.tBouton[i] = new Texte(Couleur .NOIR, "...", font1, new Point(910+130*(i-3), 40));
	}
	stop = false;
	message = new Texte[10];
	for(int i = 0 ; i < message.length ; i++){
	    message[i] = new Texte(Couleur .NOIR, "", font2, new Point(960, 590));
	    message[i].translater(0, -i*30);

	}
	nombreLigne = 0;

	highscore = new Texte(Couleur.NOIR, "HIGHSCORE", font3, new Point(960, 335));
	listeHighScore = new Texte[10];
	for(int i=0;i<5;i++){
	    listeHighScore[i] = new Texte(Couleur.NOIR, "", font4, new Point(820,310));
	    listeHighScore[i].translater(0,-i*25);
	}
	for(int i=5;i<10;i++){
	    listeHighScore[i] = new Texte(Couleur.NOIR, "", font4, new Point(1100,310));
	    listeHighScore[i].translater(0,-(i-5)*25);
	}
	
	
	/*
	//declaration des textes bouton + joystick
	this.tJoystick = new Texte(Couleur .NOIR, "...", new Font("Calibri", Font.TYPE1_FONT, 15), new Point(760, 80));
	for(int i = 0 ; i < 3 ; i++){
	    this.tBouton[i] = new Texte(Couleur .NOIR, "...", new Font("Calibri", Font.TYPE1_FONT, 15), new Point(910+130*i, 120));
	}
	for(int i = 3 ; i < 6 ; i++){
	    this.tBouton[i] = new Texte(Couleur .NOIR, "...", new Font("Calibri", Font.TYPE1_FONT, 15), new Point(910+130*(i-3), 40));
	}
	stop = false;
	message = new Texte[10];
	for(int i = 0 ; i < message.length ; i++){
	    message[i] = new Texte(Couleur .NOIR, "", new Font("Calibri", Font.TYPE1_FONT, 20), new Point(960, 590));
	    message[i].translater(0, -i*30);

	}
	nombreLigne = 0;

	highscore = new Texte(Couleur.NOIR, "HIGHSCORE", new Font("Calibri", Font.TYPE1_FONT, 25), new Point(960, 335));
	listeHighScore = new Texte[10];
	for(int i=0;i<5;i++){
	    listeHighScore[i] = new Texte(Couleur.NOIR, "", new Font("Calibri", Font.TYPE1_FONT, 17), new Point(820,310));
	    listeHighScore[i].translater(0,-i*25);
	}
	for(int i=5;i<10;i++){
	    listeHighScore[i] = new Texte(Couleur.NOIR, "", new Font("Calibri", Font.TYPE1_FONT, 17), new Point(1100,310));
	    listeHighScore[i].translater(0,-(i-5)*25);
	}*/

    }
    /**
     * Lit et affiche le contenu du fichier de description d'un jeu.
     * 
     * Cette méthode lit le fichier "description.txt" dans le répertoire
     * du jeu spécifié et affiche le texte ligne par ligne dans la boîte.
     * 
     * @param path chemin vers le répertoire du jeu contenant le fichier description.txt
     */
    public void lireFichier(String path){
	//System.out.println(path);
	String fichier =path+"/description.txt";
		
	//lecture du fichier texte	
	try{
	    InputStream ips=new FileInputStream(fichier); 
	    InputStreamReader ipsr=new InputStreamReader(ips);
	    BufferedReader br=new BufferedReader(ipsr);
	    String ligne;
	    while (/*(ligne=br.readLine())!=null &&*/stop == false){
		ligne=br.readLine();
		//System.out.println(ligne);
		if(ligne != null){
		    //changer message
					
		    message[nombreLigne].setTexte(ligne);
		    setMessage(ligne, nombreLigne);
		}else{
		    //changer message
					
		    message[nombreLigne].setTexte("");
		    setMessage("", nombreLigne);
		}
		nombreLigne++;
		if(nombreLigne >= 10){
		    stop = true;
		    nombreLigne = 0;
		}
	    }
	    stop = false;
	    br.close(); 
	}		
	catch (Exception e){
	    System.err.println(e.toString());
	}
    }

    /**
     * Lit et affiche les meilleurs scores d'un jeu.
     * 
     * Cette méthode lit le fichier "highscore" dans le répertoire du jeu
     * et affiche les 10 meilleurs scores dans l'interface.
     * 
     * @param path chemin vers le répertoire du jeu contenant le fichier highscore
     */
    public void lireHighScore(String path){
	
        for(int i=0;i<10;i++){
	    if(i==0)
		listeHighScore[i].setTexte("1er - ");
	    else
		listeHighScore[i].setTexte((i+1)+"eme - ");
	}
	
	String fichier =path+"/highscore";
	
	File f = new File(fichier);
	if(!f.exists()){
	    for(int i=0;i<10;i++)
		listeHighScore[i].setTexte("/");
	}else{
	    ArrayList<LigneHighScore> liste = HighScore.lireFichier(fichier);
	    for(int i=0;i<liste.size();i++){
		if(i==0)
		    listeHighScore[i].setTexte("1er : "+liste.get(i).getNom()+" - "+liste.get(i).getScore());
		else
		    listeHighScore[i].setTexte((i+1)+"eme : "+liste.get(i).getNom()+" -  "+liste.get(i).getScore());
	    }
	}
    }

    /**
     * Lit et configure l'affichage des contrôles d'un jeu.
     * 
     * Cette méthode lit le fichier "bouton.txt" dans le répertoire du jeu
     * pour configurer l'affichage des contrôles (joystick et boutons).
     * 
     * @param path chemin vers le répertoire du jeu contenant le fichier bouton.txt
     */
    public void lireBouton(String path){
	//System.out.println(path);
	String fichier =path+"/bouton.txt";
		
	//lecture du fichier texte	
	try{
	    InputStream ips=new FileInputStream(fichier); 
	    InputStreamReader ipsr=new InputStreamReader(ips);
	    BufferedReader br=new BufferedReader(ipsr);
	    String ligne;
	    ligne = br.readLine();
	    if(ligne == null){
		System.err.println("le fichier bouton est surement vide!");
	    }else{
		texteBouton = ligne.split(":");
		//changer le texte des boutons
		settJoystick(texteBouton[0]);
		for(int i = 0 ; i < 6 ; i++){
		    settBouton(texteBouton[i+1], i);
		}				
	    }
	}catch(Exception e){System.err.println(e);};
		
    }
    /**
     * Récupère le tableau des messages de description.
     * 
     * @return tableau des textes de description du jeu
     */
    public Texte[] getMessage(){
	return message;
    }
    /**
     * Modifie le texte d'une ligne de description spécifique.
     * 
     * @param message nouveau texte à afficher
     * @param a index de la ligne à modifier
     */
    public void setMessage(String message, int a) {
	this.message[a].setTexte(message);	
    }
    /**
     * Récupère le tableau des textures des boutons.
     * 
     * @return tableau des textures des 6 boutons de contrôle
     */
    public Texture[] getBouton(){
	return this.bouton;
    }
    /**
     * Récupère la texture du joystick.
     * 
     * @return texture du joystick de contrôle
     */
    public Texture getJoystick(){
	return this.joystick;
    }
    /**
     * Récupère le tableau des textes descriptifs des boutons.
     * 
     * @return tableau des textes expliquant le rôle de chaque bouton
     */
    public Texte[] gettBouton(){
	return this.tBouton;
    }
    /**
     * Récupère le texte descriptif du joystick.
     * 
     * @return texte expliquant le rôle du joystick
     */
    public Texte gettJoystick(){
	return this.tJoystick;
    }

    /**
     * Récupère le texte du titre "High Score".
     * 
     * @return texte du titre de la section des meilleurs scores
     */
    public Texte getHighscore(){
	return this.highscore;
    }

    /**
     * Récupère la liste des meilleurs scores.
     * 
     * @return tableau des textes affichant les 10 meilleurs scores
     */
    public Texte[] getListeHighScore(){
	return this.listeHighScore;
    }

    /**
     * Modifie le texte descriptif du joystick.
     * 
     * @param s nouveau texte à afficher pour décrire le joystick
     */
    public void settJoystick(String s){
	this.tJoystick.setTexte(s);		
    }
    /**
     * Modifie le texte descriptif d'un bouton spécifique.
     * 
     * @param s nouveau texte à afficher pour décrire le bouton
     * @param a index du bouton à modifier (0-5)
     */
    public void settBouton(String s, int a){
	this.tBouton[a].setTexte(s);		
    }
	
    /*public Texte getMessage() {
      return message;
      }
    */
	

}
