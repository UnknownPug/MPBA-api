package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

public enum CardType {
    VISA("VISA"), MASTERCARD("MASTERCARD");

    private final String type;

    CardType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
