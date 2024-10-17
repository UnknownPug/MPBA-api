package api.mpba.rastvdmy.repository;

import api.mpba.rastvdmy.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * This interface represents the repository for the UserProfile entity.
 * It extends JpaRepository to provide methods for manipulating UserProfile entities.
 * JpaRepository is a JPA-specific extension of Repository
 * that offers methods for common database operations, such as saving, deleting, and finding records.
 * It is annotated with @Repository to indicate that it serves as a data access layer.
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    /**
     * Finds a UserProfile entity by its email address.
     *
     * @param email The email address of the UserProfile to find.
     * @return An Optional containing the UserProfile if found, or empty if no UserProfile exists with the given email.
     */
    Optional<UserProfile> findByEmail(String email);
}
