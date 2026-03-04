/**
 * Classe principale de la borne d'arcade.
 * 
 * Cette classe contient le point d'entrée de l'application qui lance 
 * l'interface graphique de la borne d'arcade et démarre la boucle 
 * principale de sélection des jeux.
 * 
 * @author Lorenzo Vandenkoornhuyse
 * @version 1.0
 */
public class Main {
    
    /**
     * Point d'entrée de l'application.
     * Crée une instance de Graphique et démarre la boucle principale
     * de sélection des jeux de la borne d'arcade.
     * 
     * @param args arguments de ligne de commande
     */
    public static void main(String[] args){
        // Initialiser la configuration
        Config.init(args);
        Config.getInstance().printConfig();
        
        System.out.println("\nLancement de la borne d'arcade...\n");
        launchArcade();
    }
    
    /**
     * Lance la boucle principale de la borne d'arcade.
     */
    private static void launchArcade() {
        Graphique g = new Graphique();
        while(true){
            try{
                // Thread.sleep(250);
            }catch(Exception e){ 
                System.out.println("Erreur dans la boucle principale : " + e.getMessage()); 
            }
            g.selectionJeu();
        }
    }
}
