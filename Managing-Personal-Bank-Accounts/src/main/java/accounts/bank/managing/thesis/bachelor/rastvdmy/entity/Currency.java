package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

public enum Currency {
    CZK("CZK"), USD("USD"), EUR("EUR"), PLN("PLN"), UAH("UAH");

    private final String currency;

    Currency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return currency;
    }
}
