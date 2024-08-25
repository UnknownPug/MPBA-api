package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;

public enum PageableState {

    ASC("asc"),

    DESC("desc");

    private final String type;

    PageableState(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
