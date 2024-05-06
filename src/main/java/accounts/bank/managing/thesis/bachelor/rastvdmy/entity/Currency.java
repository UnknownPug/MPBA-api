package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

/**
 * This enum represents the currency types.
 * It contains the types: CZK, USD, EUR, PLN, and UAH.
 */
public enum Currency {
    /**
     * Represents the Czech koruna currency type.
     */
    CZK("CZK"),

    /**
     * Represents the United States dollar currency type.
     */
    USD("USD"),

    /**
     * Represents the Euro currency type.
     */
    EUR("EUR"),

    /**
     * Represents the Polish złoty currency type.
     */
    PLN("PLN"),

    /**
     * Represents the Ukrainian hryvnia currency type.
     */
    UAH("UAH");

    /**
     * The type of the currency.
     */
    private final String currency;

    /**
     * Constructor for the Currency enum.
     *
     * @param currency The type of the currency.
     */
    Currency(String currency) {
        this.currency = currency;
    }

    /**
     * Returns the type of the currency as a string.
     *
     * @return The type of the currency.
     */
    @Override
    public String toString() {
        return currency;
    }
}
