package accounts.bank.managing.thesis.bachelor.rastvdmy.repository;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.BankLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * This interface represents the repository for the BankLoan entity.
 * It extends JpaRepository to provide methods to manipulate BankLoan entities.
 * JpaRepository is a JPA specific extension of Repository which provides JPA related methods such as flushing the persistence context and deleting records in a batch.
 * It is annotated with @Repository to indicate that it's a component that directly accesses the database.
 */
@Repository
public interface BankLoanRepository extends JpaRepository<BankLoan, Long> {

    /**
     * Finds a BankLoan entity by its reference number.
     *
     * @param referenceNumber The reference number of the BankLoan entity to find.
     * @return The BankLoan entity with the given reference number, or null if no such entity exists.
     */
    BankLoan findByReferenceNumber(String referenceNumber);
}
