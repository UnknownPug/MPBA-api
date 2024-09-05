package api.mpba.rastvdmy.repository;

import api.mpba.rastvdmy.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This interface represents the repository for the Card entity.
 * It extends JpaRepository to provide methods to manipulate Card entities.
 * JpaRepository is a JPA specific extension of Repository
 * that provides JPA related methods such as flushing the persistence context and deleting records in a batch.
 * It is annotated with @Repository to indicate that it's a component that directly accesses the database.
 */
@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    @Query("SELECT c FROM Card c WHERE c.account.id = :accountId")
    List<Card> findAllByBankAccountId(UUID accountId);

    Optional<Card> findByAccountIdAndId(UUID accountId, UUID id);

    Optional<Card> findByAccountId(UUID id);
}
