package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

public enum UserRole {
    ROLE_USER("ROLE_USER"), ROLE_ADMIN("ROLE_ADMIN"), ROLE_MODERATOR("ROLE_MODERATOR");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return role;
    }
}
