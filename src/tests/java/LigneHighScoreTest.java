/**
 * Test simple pour LigneHighScore (sans JUnit)
 */
class LigneHighScoreTest {

    public static void main(String[] args) {
        System.out.println("=== Test de LigneHighScore ===");
        
        try {
            testConstructeurParDefaut();
            testConstructeurAvecParametres();
            testToString();
            
            System.out.println("✓ Tous les tests ont réussi !");
        } catch (Exception e) {
            System.err.println("✗ Erreur dans les tests : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testConstructeurParDefaut() {
        LigneHighScore ligne = new LigneHighScore();
        assert ligne.getNom().equals("AAA") : "Le nom par défaut devrait être AAA";
        assert ligne.getScore() == 0 : "Le score par défaut devrait être 0";
        System.out.println("✓ Test constructeur par défaut réussi");
    }

    private static void testConstructeurAvecParametres() {
        LigneHighScore ligne = new LigneHighScore("ABC", 1000);
        assert ligne.getNom().equals("ABC") : "Le nom devrait être ABC";
        assert ligne.getScore() == 1000 : "Le score devrait être 1000";
        System.out.println("✓ Test constructeur avec paramètres réussi");
    }

    private static void testToString() {
        LigneHighScore ligne = new LigneHighScore("XYZ", 500);
        assert ligne.toString().equals("XYZ-500") : "toString devrait retourner XYZ-500";
        System.out.println("✓ Test toString réussi");
    }
}