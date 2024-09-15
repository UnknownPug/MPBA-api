package api.mpba.rastvdmy.service.utils;

public enum PurchaseCategory {
    GROCERIES("GROCERIES"),
    CLOTHING("CLOTHING"),
    ELECTRONICS("ELECTRONICS"),
    CINEMA("CINEMA"),
    RESTAURANT("RESTAURANT"),
    CAFE("CAFE"),
    STUDY("STUDY"),
    TRANSPORT("TRANSPORT"),
    TRAVEL("TRAVEL"),
    SPORT("SPORT"),
    OTHER("OTHER");

    private final String category;

    PurchaseCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return category;
    }

    public static String getRandomCategory() {
        return PurchaseCategory.values()[(int) (Math.random() * PurchaseCategory.values().length)].toString();
    }
}
