package api.mpba.rastvdmy.repository;

import api.mpba.rastvdmy.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Query("SELECT p.senderCard.id FROM Payment p WHERE p.id = :paymentId")
    Optional<UUID> findCardIdByPaymentId(@Param("paymentId") UUID paymentId);

    @Query("SELECT p FROM Payment p WHERE p.senderAccount.id = :accountId OR p.senderCard.id IN :cardsIds")
    Optional<List<Payment>> findAllBySenderAccountIdOrSenderCardId(@Param("accountId") UUID accountId,
                                                                   @Param("cardsIds") List<UUID> cardsIds);

    Optional<Payment> findBySenderAccountIdAndId(UUID accountId, UUID paymentId);

    Optional<Payment> findBySenderCardIdAndId(UUID cardId, UUID paymentId);

}
