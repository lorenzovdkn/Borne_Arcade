import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Classe de gestion de la configuration de la borne d'arcade.
 * 
 * Permet de basculer entre le mode PRODUCTION (pour la borne) 
 * et le mode DEVELOPPEMENT (avec outils IA pour documentation).
 * 
 * Priorité de configuration :
 * 1. Argument de ligne de commande
 * 2. Variable d'environnement BORNE_MODE
 * 3. Fichier config.properties
 * 4. Valeur par défaut : PRODUCTION
 * 
 * @author Lorenzo Vandenkoornhuyse
 * @version 1.0
 */
public class Config {
    
    /**
     * Enumération des modes disponibles
     */
    public enum Mode {
        PRODUCTION,
        DEVELOPPEMENT,
        DEV
    }
    
    /** Mode actuel de l'application */
    private static Mode currentMode = Mode.PRODUCTION;
    
    /** Instance singleton */
    private static Config instance = null;
    
    /** Properties chargées depuis le fichier */
    private Properties properties;
    
    /** Chemin vers le fichier de configuration */
    private static final String CONFIG_FILE = "config.properties";
    
    /**
     * Constructeur privé (pattern Singleton).
     */
    private Config() {
        properties = new Properties();
        loadConfig();
    }
    
    /**
     * Récupère l'instance unique de Config.
     * 
     * @return l'instance de Config
     */
    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }
    
    /**
     * Charge la configuration depuis les différentes sources.
     */
    private void loadConfig() {
        // 1. Charger depuis le fichier config.properties
        loadFromFile();
        
        // 2. Override avec variable d'environnement si présente
        String envMode = System.getenv("BORNE_MODE");
        if (envMode != null && !envMode.isEmpty()) {
            setMode(envMode);
            System.out.println("[Config] Mode défini via variable d'environnement: " + currentMode);
            return;
        }
        
        // 3. Utiliser le fichier config.properties
        String fileMode = properties.getProperty("mode");
        if (fileMode != null && !fileMode.isEmpty()) {
            setMode(fileMode);
            System.out.println("[Config] Mode défini via config.properties: " + currentMode);
            return;
        }
        
        // 4. Mode par défaut
        System.out.println("[Config] Mode par défaut: " + currentMode);
    }
    
    /**
     * Charge la configuration depuis le fichier config.properties.
     */
    private void loadFromFile() {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            properties.load(fis);
        } catch (IOException e) {
            System.out.println("[Config] Fichier config.properties non trouvé, utilisation des valeurs par défaut");
        }
    }
    
    /**
     * Définit le mode à partir d'une chaîne de caractères.
     * 
     * @param modeStr le mode sous forme de chaîne
     */
    public void setMode(String modeStr) {
        try {
            String normalized = modeStr.toUpperCase().trim();
            // Gérer l'alias DEV -> DEVELOPPEMENT
            if (normalized.equals("DEV")) {
                currentMode = Mode.DEVELOPPEMENT;
            } else {
                currentMode = Mode.valueOf(normalized);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("[Config] Mode invalide: " + modeStr + ". Utilisation de PRODUCTION.");
            currentMode = Mode.PRODUCTION;
        }
    }
    
    /**
     * Définit le mode directement.
     * 
     * @param mode le mode à définir
     */
    public void setMode(Mode mode) {
        currentMode = mode;
    }
    
    /**
     * Initialise la configuration avec les arguments de ligne de commande.
     * 
     * @param args arguments de ligne de commande
     */
    public static void init(String[] args) {
        Config config = getInstance();
        
        // Parser les arguments de ligne de commande
        for (String arg : args) {
            if (arg.startsWith("--mode=")) {
                String mode = arg.substring(7);
                config.setMode(mode);
                System.out.println("[Config] Mode défini via argument: " + currentMode);
                return;
            }
            // Format alternatif : -m PRODUCTION ou --mode PRODUCTION
            if (arg.equals("-m") || arg.equals("--mode")) {
                // Le mode sera dans le prochain argument
                continue;
            }
            if (arg.equalsIgnoreCase("PRODUCTION") || 
                arg.equalsIgnoreCase("DEVELOPPEMENT") || 
                arg.equalsIgnoreCase("DEV")) {
                config.setMode(arg);
                System.out.println("[Config] Mode défini via argument: " + currentMode);
                return;
            }
        }
    }
    
    /**
     * Vérifie si l'application est en mode production.
     * 
     * @return true si en mode production
     */
    public static boolean isProduction() {
        return currentMode == Mode.PRODUCTION;
    }
    
    /**
     * Vérifie si l'application est en mode développement.
     * 
     * @return true si en mode développement
     */
    public static boolean isDeveloppement() {
        return currentMode == Mode.DEVELOPPEMENT;
    }
    
    /**
     * Récupère le mode actuel.
     * 
     * @return le mode actuel
     */
    public static Mode getMode() {
        return currentMode;
    }
    
    /**
     * Récupère une propriété du fichier de configuration.
     * 
     * @param key la clé de la propriété
     * @return la valeur de la propriété ou null si inexistante
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Récupère une propriété avec valeur par défaut.
     * 
     * @param key la clé de la propriété
     * @param defaultValue la valeur par défaut
     * @return la valeur de la propriété ou defaultValue si inexistante
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Affiche la configuration actuelle.
     */
    public void printConfig() {
        System.out.println("=== Configuration de la Borne d'Arcade ===");
        System.out.println("Mode: " + currentMode);
        System.out.println("Production: " + isProduction());
        System.out.println("Développement: " + isDeveloppement());
        System.out.println("==========================================");
    }
}
