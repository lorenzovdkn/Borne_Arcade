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
     * Supporte les modes PRODUCTION et DEVELOPPEMENT :
     * - PRODUCTION : Borne d'arcade pour jouer
     * - DEVELOPPEMENT : Avec outils IA pour documentation
     * 
     * Usage:
     *   java Main                          (mode défini dans config.properties)
     *   java Main --mode=PRODUCTION        (force le mode production)
     *   java Main --mode=DEVELOPPEMENT     (force le mode développement)
     *   BORNE_MODE=DEV java Main           (via variable d'environnement)
     * 
     * @param args arguments de ligne de commande (--mode=PRODUCTION|DEVELOPPEMENT)
     */
    public static void main(String[] args){
        // Initialiser la configuration
        Config.init(args);
        Config.getInstance().printConfig();
        
        // Vérifier le mode et démarrer le mode approprié
        if (Config.isDeveloppement()) {
            System.out.println("\n*** MODE DEVELOPPEMENT ACTIVÉ ***");
            launchDeveloppementMode();
        } else {
            System.out.println("\n*** MODE PRODUCTION ACTIVÉ ***");
            System.out.println("Lancement de la borne d'arcade...\n");
            launchProductionMode();
        }
    }
    
    /**
     * Lance le mode production (borne d'arcade normale).
     */
    private static void launchProductionMode() {
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
    
    /**
     * Lance le mode développement avec outils IA.
     */
    private static void launchDeveloppementMode() {
        // Initialiser les outils de développement
        DevTools devTools = new DevTools();
        devTools.printDebugInfo();
        
        // Vérifier si la génération automatique est activée
        Config config = Config.getInstance();
        if (Boolean.parseBoolean(config.getProperty("dev.ai.auto_generate_doc", "true"))) {
            System.out.println("[DEV] Génération automatique de la documentation...");
            devTools.generateDocumentation();
            System.out.println();
        }
        
        // Lancer le mode normal avec des logs supplémentaires
        System.out.println("[DEV] Lancement de l'interface graphique...\n");
        Graphique g = new Graphique();
        while(true){
            try{
                // Thread.sleep(250);
            }catch(Exception e){ 
                System.err.println("[DEV] Erreur dans la boucle principale : " + e.getMessage());
                e.printStackTrace();
            }
            g.selectionJeu();
        }
    }
}
