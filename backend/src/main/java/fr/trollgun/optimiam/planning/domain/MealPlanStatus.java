package fr.trollgun.optimiam.planning.domain;

public enum MealPlanStatus {
    PLANNED("Planifié"),
    COOKED("Cuisiné"),
    CANCELLED("Annulé");

    private final String label;

    MealPlanStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
