package api.mpba.rastvdmy.entity.enums;

/**
 * This enum represents the status a user can have in the system.
 * It contains the statuses: STATUS_DEFAULT, STATUS_BLOCKED, and STATUS_UNBLOCKED.
 */
public enum UserStatus {

    /**
     * Represents the default status of a user.
     */
    STATUS_DEFAULT("STATUS_DEFAULT"),

    /**
     * Represents the status when a user is blocked.
     */
    STATUS_BLOCKED("STATUS_BLOCKED"),

    /**
     * Represents the status when a user is unblocked.
     */
    STATUS_UNBLOCKED("STATUS_UNBLOCKED");

    /**
     * The status of the user.
     */
    private final String status;

    /**
     * Constructor for the UserStatus enum.
     *
     * @param status The status of the user.
     */
    UserStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the status of the user as a string.
     *
     * @return The status of the user.
     */
    @Override
    public String toString() {
        return status;
    }
}
