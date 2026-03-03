import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire des outils de développement et IA.
 * 
 * Cette classe fournit des fonctionnalités pour le mode développement :
 * - Génération automatique de documentation
 * - Analyse de code
 * - Outils de debugging avancés
 * 
 * Activé uniquement en mode DEVELOPPEMENT.
 * 
 * @author Lorenzo Vandenkoornhuyse
 * @version 1.0
 */
public class DevTools {
    
    /** Répertoire de sortie pour la documentation générée */
    private String outputDir;
    
    /** Configuration */
    private Config config;
    
    /**
     * Constructeur.
     */
    public DevTools() {
        config = Config.getInstance();
        outputDir = config.getProperty("dev.doc.output_dir", "src/docs/generated");
        initializeOutputDirectory();
    }
    
    /**
     * Initialise le répertoire de sortie.
     */
    private void initializeOutputDirectory() {
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("[DevTools] Répertoire créé: " + outputDir);
        }
    }
    
    /**
     * Vérifie si les outils de développement sont activés.
     * 
     * @return true si activés
     */
    public boolean isEnabled() {
        return Config.isDeveloppement() && 
               Boolean.parseBoolean(config.getProperty("dev.ai.enabled", "true"));
    }
    
    /**
     * Affiche les informations de debug.
     */
    public void printDebugInfo() {
        if (!isEnabled()) return;
        
        System.out.println("\n[DevTools] === Informations de Debug ===");
        System.out.println("[DevTools] Configuration chargée");
        System.out.println("[DevTools] Répertoire de sortie: " + outputDir);
        System.out.println("[DevTools] IA activée: " + config.getProperty("dev.ai.enabled"));
        System.out.println("[DevTools] Auto-génération doc: " + config.getProperty("dev.ai.auto_generate_doc"));
        System.out.println("[DevTools] =====================================\n");
    }

    /**
     * Génère la documentation en simulant le processus de création.
     * Vérifie si les outils de développement sont activés avant de commencer.
     * Affiche des messages d'état et simule un délai de génération.
     * En cas d'interruption, affiche une erreur correspondante.
     * Le chemin d'export est indiqué à la fin de la génération.
     */

    public void generateDocumentation() {
        if (!isEnabled()) return;
        if (!Boolean.parseBoolean(config.getProperty("dev.doc.enabled", "true"))) {
            System.out.println("[DevTools] Génération de documentation désactivée (dev.doc.enabled=false)");
            return;
        }

        boolean interactive = Boolean.parseBoolean(config.getProperty("dev.doc.interactive", "false"));
        boolean applyChanges = Boolean.parseBoolean(config.getProperty("dev.doc.apply", "false"));

        List<String> command = new ArrayList<>();
        command.add("python3");
        command.add("src/IA/API/doc_generator.py");

        if (interactive) {
            command.add("--interactive");
        } else if (applyChanges) {
            command.add("--apply");
        }
        
        System.out.println("[DevTools] Génération de la documentation en cours...");
        System.out.println("[DevTools] Commande: " + String.join(" ", command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(new File("."));
        processBuilder.inheritIO();

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("[DevTools] Documentation générée avec succès.");
            } else {
                System.err.println("[DevTools] Échec de la génération de documentation (code=" + exitCode + ").");
            }
        } catch (IOException e) {
            System.err.println("[DevTools] Impossible de lancer doc_generator.py: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[DevTools] Erreur lors de la génération de documentation: " + e.getMessage());
        }
    }
}
