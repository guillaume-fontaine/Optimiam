package fr.trollgun.optimiam.product.domain;

public enum Unit {
    KG("Kilogramme", "kg"),
    G("Gramme", "g"),
    L("Litre", "L"),
    ML("Millilitre", "ml"),
    PIECE("Pièce", "pièce");

    private final String label;
    private final String symbol;

    Unit(String label, String symbol) {
        this.label = label;
        this.symbol = symbol;
    }

    public String getLabel() {
        return label;
    }

    public String getSymbol() {
        return symbol;
    }
}
