package api.mpba.rastvdmy.entity.enums;

/**
 * This enum represents the status of a card.
 * It contains the statuses: STATUS_CARD_BLOCKED, STATUS_CARD_UNBLOCKED, and STATUS_CARD_DEFAULT.
 */
public enum CardStatus {

    /**
     * The status when a card is blocked.
     */
    STATUS_CARD_BLOCKED("STATUS_CARD_BLOCKED"),

    /**
     * The status when a card is unblocked.
     */
    STATUS_CARD_UNBLOCKED("STATUS_CARD_UNBLOCKED"),

    /**
     * The default status of a card.
     */
    STATUS_CARD_DEFAULT("STATUS_CARD_DEFAULT");

    /**
     * The status of the card.
     */
    private final String status;

    /**
     * Constructor for the CardStatus enum.
     *
     * @param status The status of the card.
     */
    CardStatus(String status) {
        this.status = status;
    }

    /**
     * Returns a random CardStatus.
     *
     * @return A random status from the enum values.
     */
    public static CardStatus getRandomStatus() {
        return values()[(int) (Math.random() * values().length)];
    }

    /**
     * Returns the status of the card as a string.
     *
     * @return The status of the card.
     */
    @Override
    public String toString() {
        return status;
    }
}
