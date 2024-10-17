package api.mpba.rastvdmy.repository;

import api.mpba.rastvdmy.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This interface represents the repository for the Payment entity.
 * It extends JpaRepository to provide methods for manipulating Payment entities.
 * JpaRepository is a JPA-specific extension of Repository
 * that provides methods for common database operations, such as saving, deleting, and finding records.
 * It is annotated with @Repository to indicate that it serves as a data access layer.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    /**
     * Finds the card ID associated with a specific payment by its payment ID.
     *
     * @param paymentId The ID of the payment for which to find the card ID.
     * @return An Optional containing the card ID if found, or empty if not found.
     */
    @Query("SELECT p.senderCard.id FROM Payment p WHERE p.id = :paymentId")
    Optional<UUID> findCardIdByPaymentId(@Param("paymentId") UUID paymentId);

    /**
     * Finds all payments associated with a specific sender account ID or any of the given sender card IDs.
     *
     * @param accountId The ID of the sender account.
     * @param cardsIds  A list of sender card IDs.
     * @return An Optional containing a list of payments that match the criteria, or empty if none are found.
     */
    @Query("SELECT p FROM Payment p WHERE p.senderAccount.id = :accountId OR p.senderCard.id IN :cardsIds")
    Optional<List<Payment>> findAllBySenderAccountIdOrSenderCardId(@Param("accountId") UUID accountId,
                                                                   @Param("cardsIds") List<UUID> cardsIds);

    /**
     * Finds a payment by the sender account ID and the payment ID.
     *
     * @param accountId The ID of the sender account.
     * @param paymentId The ID of the payment to find.
     * @return An Optional containing the payment if found, or empty if not found.
     */
    Optional<Payment> findBySenderAccountIdAndId(UUID accountId, UUID paymentId);

    /**
     * Finds a payment by the sender card ID and the payment ID.
     *
     * @param cardId    The ID of the sender card.
     * @param paymentId The ID of the payment to find.
     * @return An Optional containing the payment if found, or empty if not found.
     */
    Optional<Payment> findBySenderCardIdAndId(UUID cardId, UUID paymentId);
}
