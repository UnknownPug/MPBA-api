package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

public enum UserVisibility {
    STATUS_ONLINE("STATUS_ONLINE"), STATUS_OFFLINE("STATUS_OFFLINE");

    private final String status;

    UserVisibility(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }
}
