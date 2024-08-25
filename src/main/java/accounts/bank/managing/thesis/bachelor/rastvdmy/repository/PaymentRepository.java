package accounts.bank.managing.thesis.bachelor.rastvdmy.repository;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    List<Payment> findAllBySenderAccountId(UUID accountId);

    Page<Payment> findAllBySenderAccountId(UUID accountId, Pageable pageable);

    Payment findBySenderAccountId(UUID id);
}
