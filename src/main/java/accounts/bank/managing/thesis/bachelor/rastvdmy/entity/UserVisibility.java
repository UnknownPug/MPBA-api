package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

/**
 * This enum represents the visibility status a user can have in the system.
 * It contains the statuses: STATUS_ONLINE and STATUS_OFFLINE.
 */
public enum UserVisibility {
    /**
     * Represents the status when a user is online.
     */
    STATUS_ONLINE("STATUS_ONLINE"),

    /**
     * Represents the status when a user is offline.
     */
    STATUS_OFFLINE("STATUS_OFFLINE");

    /**
     * The visibility status of the user.
     */
    private final String status;

    /**
     * Constructor for the UserVisibility enum.
     *
     * @param status The visibility status of the user.
     */
    UserVisibility(String status) {
        this.status = status;
    }

    /**
     * Returns the visibility status of the user as a string.
     *
     * @return The visibility status of the user.
     */
    @Override
    public String toString() {
        return status;
    }
}
