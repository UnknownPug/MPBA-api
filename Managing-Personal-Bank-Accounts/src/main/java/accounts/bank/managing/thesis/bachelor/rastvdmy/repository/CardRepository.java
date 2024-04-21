package accounts.bank.managing.thesis.bachelor.rastvdmy.repository;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * This interface represents the repository for the Card entity.
 * It extends JpaRepository to provide methods to manipulate Card entities.
 * JpaRepository is a JPA specific extension of Repository which provides JPA related methods such as flushing the persistence context and deleting records in a batch.
 * It is annotated with @Repository to indicate that it's a component that directly accesses the database.
 */
@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    /**
     * Finds a Card entity by its card number.
     *
     * @param receiverCardNumber The card number of the Card entity to find.
     * @return The Card entity with the given card number, or null if no such entity exists.
     */
    Card findByCardNumber(String receiverCardNumber);

    /**
     * Finds a Card entity by its associated loan ID.
     *
     * @param loanId The loan ID associated with the Card entity to find.
     * @return The Card entity with the given loan ID, or null if no such entity exists.
     */
    Card findByCardLoanId(Long loanId);
}
