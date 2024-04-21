package accounts.bank.managing.thesis.bachelor.rastvdmy.repository;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.User;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * This interface represents the repository for the User entity.
 * It extends JpaRepository to provide methods to manipulate User entities.
 * JpaRepository is a JPA specific extension of Repository
 * that provides JPA related methods such as flushing the persistence context and deleting records in a batch.
 * It is annotated with @Repository to indicate that it's a component that directly accesses the database.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Checks if a User entity exists by its email.
     *
     * @param email The email of the User entity to check.
     * @return true if a User entity with the given email exists, false otherwise.
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a User entity exists by its phone number.
     *
     * @param phoneNumber The phone number of the User entity to check.
     * @return true if a User entity with the given phone number exists, false otherwise.
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Checks if a User entity exists by its user role.
     *
     * @param userRole The user role of the User entity to check.
     * @return true if a User entity with the given user role exists, false otherwise.
     */
    boolean existsByUserRole(UserRole userRole);

    /**
     * Finds a User entity by its email.
     *
     * @param email The email of the User entity to find.
     * @return The User entity with the given email, or null if no such entity exists.
     */
    User findByEmail(String email);

    /**
     * Finds a User entity by its associated bank loan ID.
     *
     * @param loanId The bank loan ID associated with the User entity to find.
     * @return The User entity with the given bank loan ID, or null if no such entity exists.
     */
    User findByBankLoanId(Long loanId);
}
