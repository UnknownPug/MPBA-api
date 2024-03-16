package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

public enum FinancialStatus {
    RECEIVED("RECEIVED"), DENIED("DENIED");

    private final String status;

    FinancialStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }
}
