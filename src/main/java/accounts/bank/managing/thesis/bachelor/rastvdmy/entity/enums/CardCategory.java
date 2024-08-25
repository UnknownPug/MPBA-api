package accounts.bank.managing.thesis.bachelor.rastvdmy.entity.enums;

/**
 * This enum represents the category of a card.
 * It contains the categories: DEBIT, PREPAID and CREDIT.
 */
public enum CardCategory {

    /**
     * Represents a DEBIT card category.
     */
    DEBIT("DEBIT");


    /**
     * The category of the card.
     */
    private final String category;

    /**
     * Constructor for the CardCategory enum.
     *
     * @param category The category of the card.
     */
    CardCategory(String category) {
        this.category = category;
    }

    /**
     * Returns the category of the card as a string.
     *
     * @return The category of the card.
     */
    @Override
    public String toString() {
        return category;
    }
}
