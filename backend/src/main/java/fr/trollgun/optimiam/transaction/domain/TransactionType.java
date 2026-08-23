package fr.trollgun.optimiam.transaction.domain;

public enum TransactionType {
    ENTRY("Entrée en stock"),
    EXIT("Sortie manuelle"),
    CONSUMPTION("Consommation recette / repas"),
    LOSS("Perte / Gaspillage"),
    ADJUSTMENT("Ajustement inventaire");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
