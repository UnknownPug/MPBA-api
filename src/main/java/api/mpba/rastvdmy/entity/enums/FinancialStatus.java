package api.mpba.rastvdmy.entity.enums;

/**
 * This enum represents the financial status.
 * It contains the statuses - RECEIVED and DENIED.
 */
public enum FinancialStatus {

    /**
     * Represents the status when a financial transaction is received.
     */
    RECEIVED("RECEIVED"),

    /**
     * Represents the status when a financial transaction is denied.
     */
    DENIED("DENIED");

    /**
     * The status of the financial transaction.
     */
    private final String status;

    /**
     * Constructor for the FinancialStatus enum.
     *
     * @param status The status of the financial transaction.
     */
    FinancialStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the status of the financial transaction as a string.
     *
     * @return The status of the financial transaction.
     */
    @Override
    public String toString() {
        return status;
    }
}
