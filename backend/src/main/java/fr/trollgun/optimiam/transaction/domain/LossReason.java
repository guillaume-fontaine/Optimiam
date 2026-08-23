package fr.trollgun.optimiam.transaction.domain;

public enum LossReason {
    EXPIRED("Date de péremption dépassée (DLC)"),
    SPOILED("Produit abîmé ou moisi"),
    OVERCOOKED("Surplus non consommé / Trop préparé"),
    DAMAGED("Emballage détérioré"),
    OTHER("Autre motif");

    private final String label;

    LossReason(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
