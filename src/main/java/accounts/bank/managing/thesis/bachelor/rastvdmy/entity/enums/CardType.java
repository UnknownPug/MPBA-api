package accounts.bank.managing.thesis.bachelor.rastvdmy.entity.enums;

/**
 * This enum represents the type of card.
 * It contains the types: VISA and MASTERCARD.
 */
public enum CardType {

    /**
     * Represents a VISA card type.
     */
    VISA("VISA"),

    /**
     * Represents a MASTERCARD card type.
     */
    MASTERCARD("MASTERCARD");

    /**
     * The type of the card.
     */
    private final String type;

    /**
     * Constructor for the CardType enum.
     *
     * @param type The type of the card.
     */
    CardType(String type) {
        this.type = type;
    }

    /**
     * Returns the type of the card as a string.
     *
     * @return The type of the card.
     */
    @Override
    public String toString() {
        return type;
    }

    public static CardType getRandomCardType() {
        return values()[(int) (Math.random() * values().length)];
    }
}
