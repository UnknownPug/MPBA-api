package api.mpba.rastvdmy.service.impl;

/**
 * Enum representing various categories of purchases.
 */
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

    /**
     * Constructor for PurchaseCategory enum.
     *
     * @param category the name of the purchase category
     */
    PurchaseCategory(String category) {
        this.category = category;
    }

    /**
     * Returns the string representation of the purchase category.
     *
     * @return the category name
     */
    @Override
    public String toString() {
        return category;
    }

    /**
     * Returns a random purchase category from the enum.
     *
     * @return a random PurchaseCategory as a string
     */
    public static String getRandomCategory() {
        return PurchaseCategory.values()[(int) (Math.random() * PurchaseCategory.values().length)].toString();
    }
}
