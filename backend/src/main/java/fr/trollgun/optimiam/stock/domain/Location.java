package fr.trollgun.optimiam.stock.domain;

public enum Location {
    FRIDGE("Réfrigérateur", "kitchen"),
    FREEZER("Congélateur", "ac_unit"),
    PANTRY("Placard / Garde-manger", "inventory"),
    OTHER("Autre", "shelves");

    private final String label;
    private final String icon;

    Location(String label, String icon) {
        this.label = label;
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public String getIcon() {
        return icon;
    }
}
