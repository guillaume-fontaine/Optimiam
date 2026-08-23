package fr.trollgun.optimiam.stock.domain;

public enum StockStatus {
    AVAILABLE("Disponible"),
    EXPIRING_SOON("À consommer rapidement"),
    EXPIRED("Périmé"),
    CONSUMED("Consommé"),
    DISCARDED("Jeté / Perte");

    private final String label;

    StockStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
