package api.mpba.rastvdmy.controller;

/**
 * Enum representing the sorting order for pageable requests.
 * <p>
 * This enum defines two states: ASC (ascending) and DESC (descending) for
 * sorting data when implementing pagination functionality.
 * </p>
 */
public enum PageableState {

    /**
     * Represents ascending order.
     */
    ASC("asc"),

    /**
     * Represents descending order.
     */
    DESC("desc");

    private final String type;

    /**
     * Constructor for the PageableState enum.
     *
     * @param type The string representation of the sorting order.
     */
    PageableState(String type) {
        this.type = type;
    }

    /**
     * Returns the string representation of the sorting order.
     *
     * @return The sorting order as a string.
     */
    @Override
    public String toString() {
        return type;
    }
}
