package api.mpba.rastvdmy.repository;

import api.mpba.rastvdmy.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing cards in the database.
 * It extends JpaRepository, providing basic CRUD operations and JPA-related methods.
 */
@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    /**
     * Finds all cards associated with a specific bank account ID.
     *
     * @param id The ID of the bank account.
     * @return An optional list of cards linked to the provided bank account ID.
     */
    Optional<List<Card>> findAllByAccountId(UUID id);

    /**
     * Finds a specific card by its account ID and card ID.
     *
     * @param accountId The ID of the bank account associated with the card.
     * @param cardId    The ID of the card to find.
     * @return An optional card matching the provided account ID and card ID.
     */
    Optional<Card> findByAccountIdAndId(UUID accountId, UUID cardId);
}
