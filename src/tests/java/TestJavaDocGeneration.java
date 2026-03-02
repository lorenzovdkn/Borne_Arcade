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

    public String getFormattedInfo() {
        return "Name: " + this.name + ", Value: " + this.value;
    }

    public boolean isValuePositive() {
        return this.value > 0;
    }

    public void incrementValue(int amount) {
        this.value += amount;
    }

    public void resetToDefaults() {
        this.name = "Test";
        this.value = 0;
    }
}
