import java.io.File;

public class GameLauncher {
    
    public enum GameType {
        SHELL, PYTHON, JAVA, LOVE, UNKNOWN
    }
    
    /**
     * Détermine le type de jeu en fonction des fichiers présents dans le répertoire spécifié.
     * La priorité est donnée aux types suivants dans l'ordre : shell, LOVE2D, Python, Java.
     * 
     * @param gameDir Le répertoire contenant les fichiers du jeu.
     * @return Le type de jeu détecté (SHELL, LOVE, PYTHON, JAVA ou UNKNOWN).
     */

    public static GameType detectGameType(File gameDir) {
        
        File[] shellScripts = gameDir.listFiles((dir, name) -> name.endsWith(".sh"));
        if (shellScripts != null && shellScripts.length > 0) {
            return GameType.SHELL;
        }
        
        // Priorité 2 : LOVE2D
        if (new File(gameDir, "main.lua").exists() || 
            new File(gameDir, "conf.lua").exists() ||
            gameDir.getName().endsWith(".love")) {
            return GameType.LOVE;
        }
        
        // Priorité 3 : Python (recherche de dossiers avec .py)
        if (new File(gameDir, "src").isDirectory() || 
            new File(gameDir, "app").isDirectory() ||
            hasFileWithExtension(gameDir, ".py")) {
            return GameType.PYTHON;
        }
        
        // Priorité 4 : Java (n'importe quel fichier .java)
        if (hasFileWithExtension(gameDir, ".java") ||
            hasFileWithExtension(gameDir, ".class")) {
            return GameType.JAVA;
        }
        
        return GameType.UNKNOWN;
    }
    
    /**
     * Vérifie si un répertoire contient au moins un fichier avec l'extension spécifiée.
     * @param dir le répertoire dans lequel rechercher
     * @param extension l'extension de fichier à rechercher (ex: ".java", ".sh")
     * @return true si au moins un fichier avec cette extension existe, false sinon
     */
    private static boolean hasFileWithExtension(File dir, String extension) {
        File[] files = dir.listFiles((d, name) -> name.endsWith(extension));
        return files != null && files.length > 0;
    }
    
    /**
     * Recherche un script shell dans le répertoire du jeu.
     * @param gameDir le répertoire du jeu où chercher le script
     * @return le premier fichier .sh trouvé, ou null si aucun script n'existe
     */
    private static File findShellScript(File gameDir) {
        File[] shellScripts = gameDir.listFiles((dir, name) -> name.endsWith(".sh"));
        return (shellScripts != null && shellScripts.length > 0) ? shellScripts[0] : null;
    }
    
    /**
     * Recherche le nom de la classe principale Java dans le répertoire du jeu.
     * Cherche d'abord Main.java ou Main.class, sinon prend le premier fichier Java trouvé.
     * @param gameDir le répertoire du jeu où chercher la classe principale
     * @return le nom de la classe principale (sans extension)
     */
    private static String findMainJavaClass(File gameDir) {
        // Chercher Main.java en priorité
        if (new File(gameDir, "Main.java").exists() || 
            new File(gameDir, "Main.class").exists()) {
            return "Main";
        }
        
        // Sinon, chercher un fichier Java qui contient "main"
        File[] javaFiles = gameDir.listFiles((dir, name) -> 
            name.endsWith(".java") && !name.contains("$"));
        
        if (javaFiles != null && javaFiles.length > 0) {
            // Prendre le premier fichier Java (souvent nommé comme le jeu)
            String fileName = javaFiles[0].getName();
            return fileName.substring(0, fileName.length() - 5); // Enlever .java
        }
        
        return "Main";
    }
    
    /**
     * Lance un jeu à partir de son nom ou chemin.
     * Détecte automatiquement le type de jeu (Shell, Python, Java, Lua) et exécute la commande appropriée.
     * @param gameName le nom du jeu ou le chemin vers le répertoire du jeu
     * @return le processus du jeu lancé
     * @throws Exception si le jeu est introuvable ou de type inconnu
     */
    public static Process launchGame(String gameName) throws Exception {
        
        File gameDir;
        
        // Si le chemin commence déjà par "src/projets/", l'utiliser tel quel
        if (gameName.startsWith("src/projets/")) {
            gameDir = new File(gameName);
        } else {
            gameDir = new File("src/projets/" + gameName);
        }
        
        System.out.println("Chemin final : " + gameDir.getAbsolutePath());
        
        if (!gameDir.exists()) {
            throw new Exception("Jeu introuvable : " + gameDir.getAbsolutePath());
        }
        
        GameType type = detectGameType(gameDir);
        
        ProcessBuilder pb;
        
        switch (type) {
            case SHELL:
                File shellScript = findShellScript(gameDir);
                System.out.println("Commande : ./" + shellScript.getName());
                pb = new ProcessBuilder("/bin/bash", "./" + shellScript.getName());
                break;
                
            case PYTHON:
                // Essayer src/ d'abord, sinon app/, sinon main.py, sinon racine
                if (new File(gameDir, "src").isDirectory()) {
                    System.out.println("Commande : python3 ./src");
                    pb = new ProcessBuilder("python3", "./src");
                } else if (new File(gameDir, "app").isDirectory()) {
                    System.out.println("Commande : python3 app/game.py");
                    pb = new ProcessBuilder("python3", "app/game.py");
                } else if (new File(gameDir, "main.py").exists()) {
                    System.out.println("Commande : python3 main.py");
                    pb = new ProcessBuilder("python3", "main.py");
                } else {
                    System.out.println("Commande : python3 .");
                    pb = new ProcessBuilder("python3", ".");
                }
                break;
                
            case LOVE:
                System.out.println("Commande : love .");
                pb = new ProcessBuilder("love", ".");
                break;
                
            case JAVA:
                String mainClass = findMainJavaClass(gameDir);
                
                // Compiler le jeu si nécessaire
                if (!new File(gameDir, mainClass + ".class").exists()) {
                    System.out.println("Compilation du jeu Java...");
                    ProcessBuilder compileBuilder = new ProcessBuilder(
                        "/bin/sh", "-c", "javac -encoding UTF-8 -cp .:../../main/java:../../../lib:../../../src/resources *.java"
                    );
                    compileBuilder.directory(gameDir);
                    compileBuilder.inheritIO();
                    Process compileProcess = compileBuilder.start();
                    int compileExitCode = compileProcess.waitFor();
                    if (compileExitCode != 0) {
                        throw new Exception("Échec de la compilation du jeu Java");
                    }
                    System.out.println("Compilation terminée");
                }
                
                pb = new ProcessBuilder("java", "-cp", ".:../../main/java:../../../lib:../../../src/resources", mainClass);
                break;
                
            default:
                throw new Exception("Type de jeu non reconnu : " + gameName);
        }
        
        pb.directory(gameDir);
        pb.inheritIO();
        
        System.out.println("Lancement du processus...");
        Process process = pb.start();
        System.out.println("Processus lancé !");
        
        return process;
    }
}
