package fr.trollgun.optimiam.shopping.domain;

public enum ShoppingListStatus {
    ACTIVE("Active"),
    COMPLETED("Terminée / Achetée"),
    ARCHIVED("Archivée");

    private final String label;

    ShoppingListStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
