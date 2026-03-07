/**
 * Classe de test pour la génération automatique de documentation Javadoc.
 * 
 * Cette classe contient intentionnellement des fonctions sans documentation
 * pour tester le système de détection et génération automatique.
 */
public class TestJavaDocGeneration {

    private String name;
    private int value;

    public TestJavaDocGeneration() {
        this.name = "Test";
        this.value = 0;
    }

    /**
     * Retourne une représentation formatée des informations de l'objet.
     * @return une chaîne au format "Name: [name], Value: [value]"
     */
    public String getFormattedInfo() {
        return "Name: " + this.name + ", Value: " + this.value;
    }

    /**
     * Vérifie si la valeur est strictement positive.
     * @return true si value > 0, false sinon
     */
    public boolean isValuePositive() {
        return this.value > 0;
    }

    /**
     * Incrémente la valeur par le montant spécifié.
     * @param amount le montant à ajouter à la valeur actuelle
     */
    public void incrementValue(int amount) {
        this.value += amount;
    }

    /**
     * Réinitialise les attributs aux valeurs par défaut.
     * Le nom devient "Test" et la valeur devient 0.
     */
    public void resetToDefaults() {
        this.name = "Test";
        this.value = 0;
    }
}
