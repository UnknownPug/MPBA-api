package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

public enum UserRole {
    ROLE_USER("USER"), ROLE_ADMIN("ADMIN"), ROLE_MODERATOR("MODERATOR");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return role;
    }
}
