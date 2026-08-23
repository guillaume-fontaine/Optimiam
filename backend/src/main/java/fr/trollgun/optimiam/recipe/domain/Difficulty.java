package fr.trollgun.optimiam.recipe.domain;

public enum Difficulty {
    EASY("Facile"),
    MEDIUM("Moyen"),
    HARD("Difficile");

    private final String label;

    Difficulty(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
