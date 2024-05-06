package accounts.bank.managing.thesis.bachelor.rastvdmy.repository;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Card;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * This interface represents the repository for the Deposit entity.
 * It extends JpaRepository to provide methods to manipulate Deposit entities.
 * JpaRepository is a JPA specific extension of Repository
 * that provides JPA related methods such as flushing the persistence context and deleting records in a batch.
 * It is annotated with @Repository to indicate that it's a component that directly accesses the database.
 */
@Repository
public interface DepositRepository extends JpaRepository<Deposit, Long> {

    /**
     * Checks if a Deposit entity exists by its reference number.
     *
     * @param referenceNumber The reference number of the Deposit entity to check.
     * @return true if a Deposit entity with the given reference number exists, false otherwise.
     */
    boolean existsByReferenceNumber(String referenceNumber);

    /**
     * Checks if a Deposit entity exists by its associated Card entity.
     *
     * @param card The Card entity associated with the Deposit entity to check.
     * @return true if a Deposit entity with the given Card entity exists, false otherwise.
     */
    boolean existsByCardDeposit(Card card);
}
