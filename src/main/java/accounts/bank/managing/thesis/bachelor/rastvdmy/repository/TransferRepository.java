package accounts.bank.managing.thesis.bachelor.rastvdmy.repository;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * This interface represents the repository for the Transfer entity.
 * It extends JpaRepository to provide methods to manipulate Transfer entities.
 * JpaRepository is a JPA specific extension of Repository
 * that provides JPA related methods such as flushing the persistence context and deleting records in a batch.
 * It is annotated with @Repository to indicate that it's a component that directly accesses the database.
 */
@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    /**
     * Finds a Transfer entity by its reference number.
     *
     * @param referenceNumber The reference number of the Transfer entity to find.
     * @return The Transfer entity with the given reference number, or null if no such entity exists.
     */
    Transfer findByReferenceNumber(String referenceNumber);

    /**
     * Checks if a Transfer entity exists by its reference number.
     *
     * @param referenceNumber The reference number of the Transfer entity to check.
     * @return true if a Transfer entity with the given reference number exists, false otherwise.
     */
    boolean existsByReferenceNumber(String referenceNumber);
}
