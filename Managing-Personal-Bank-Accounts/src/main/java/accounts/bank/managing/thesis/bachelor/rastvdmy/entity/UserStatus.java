package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

public enum UserStatus {
    STATUS_DEFAULT("STATUS_DEFAULT"),
    STATUS_BLOCKED("STATUS_BLOCKED"),
    STATUS_UNBLOCKED("STATUS_UNBLOCKED");

    private final String status;

    UserStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }
}
