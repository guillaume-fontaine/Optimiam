package fr.trollgun.optimiam.planning.domain;

public enum MealType {
    BREAKFAST("Petit-déjeuner", "free_breakfast", 1),
    LUNCH("Déjeuner (Midi)", "wb_sunny", 2),
    SNACK("Goûter / Collation", "cookie", 3),
    DINNER("Dîner (Soir)", "nights_stay", 4);

    private final String label;
    private final String icon;
    private final int displayOrder;

    MealType(String label, String icon, int displayOrder) {
        this.label = label;
        this.icon = icon;
        this.displayOrder = displayOrder;
    }

    public String getLabel() {
        return label;
    }

    public String getIcon() {
        return icon;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}
