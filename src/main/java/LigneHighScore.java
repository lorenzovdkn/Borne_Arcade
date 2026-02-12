/**
 * Classe représentant une ligne du classement des meilleurs scores.
 * 
 * Cette classe encapsule un nom de joueur et un score, et fournit
 * des méthodes pour créer, comparer et formater les entrées du classement.
 */
class LigneHighScore{
    /** Nom du joueur (3 caractères maximum) */
    private String nom;
    
    /** Score réalisé par le joueur */
    private int score;

    /**
     * Constructeur par défaut.
     * Crée une ligne avec le nom "AAA" et un score de 0.
     */
    public LigneHighScore(){
	nom="AAA";
	score=0;
    }

    /**
     * Constructeur avec nom et score.
     * 
     * @param nnom nom du joueur (limité à 3 caractères)
     * @param sscore score du joueur (doit être positif)
     */
    public LigneHighScore(String nnom, int sscore){
	if(nnom.length()>3)
	    nnom="AAA";
	else
	    nom=new String(nnom);
	if(sscore<0)
	    score=0;
	else
	    score=sscore;
    }

    /**
     * Constructeur de copie.
     * 
     * @param l ligne de high score à copier
     */
    public LigneHighScore(LigneHighScore l){
	nom=new String(l.nom);
	score=l.score;
    }

    /**
     * Constructeur à partir d'une chaîne formatée.
     * 
     * Parse une chaîne au format "nom-score" pour créer une ligne de high score.
     * Si le format est incorrect, initialise avec des valeurs par défaut.
     * 
     * @param str chaîne au format "nom-score" à parser
     */
    public LigneHighScore(String str){
	String[] tab = str.split("-");
	if(tab.length!=2){
	    nom = "AAA";
	    score=0;
	}else{
	    nom=new String(tab[0]);
	    score = Integer.parseInt(tab[1]);
	}
	    
    }

    /**
     * Récupère le score du joueur.
     * 
     * @return le score réalisé par le joueur
     */
    public int getScore(){
	return score;
    }

    /**
     * Récupère le nom du joueur.
     * 
     * @return le nom du joueur (maximum 3 caractères)
     */
    public String getNom(){
	return nom;
    }

    /**
     * Convertit la ligne de high score en chaîne de caractères.
     * 
     * @return une représentation textuelle au format "nom-score"
     */
    public String toString(){
	return nom+"-"+score;
    }
}
