package accounts.bank.managing.thesis.bachelor.rastvdmy.entity.enums;

/**
 * This enum represents the types of payments that can be made in the system.
 * It contains the types: GATEWAY and BANK_TRANSFER.
 */
public enum PaymentType {

    /**
     * Represents the payment type when a payment is made through a gateway.
     */
    CARD_PAYMENT("CARD_PAYMENT"),

    /**
     * Represents the payment type when a payment is made through a bank transfer.
     */
    BANK_TRANSFER("BANK_TRANSFER");

    /**
     * The type of the payment.
     */
    private final String type;

    /**
     * Constructor for the PaymentType enum.
     *
     * @param type The type of the payment.
     */
    PaymentType(String type) {
        this.type = type;
    }

    /**
     * Returns the type of the payment as a string.
     *
     * @return The type of the payment.
     */
    @Override
    public String toString() {
        return type;
    }
}
