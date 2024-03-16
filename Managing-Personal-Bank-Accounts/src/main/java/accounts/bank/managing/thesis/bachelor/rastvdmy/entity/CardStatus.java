package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

public enum CardStatus {
    STATUS_CARD_BLOCKED("STATUS_CARD_BLOCKED"), STATUS_CARD_UNBLOCKED("STATUS_CARD_UNBLOCKED");

    private final String status;

    CardStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }
}
