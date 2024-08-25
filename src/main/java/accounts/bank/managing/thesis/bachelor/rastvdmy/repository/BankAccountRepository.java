package accounts.bank.managing.thesis.bachelor.rastvdmy.repository;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, String> {

    List<BankAccount> findAllByBankIdentityId(UUID bankIdentityId);

    BankAccount findByBankIdentityIdAndId(UUID bankIdentity, UUID id);

    Optional<BankAccount> findByBankIdentityId(UUID id);

    Optional<BankAccount> findByAccountNumber(String recipientNumber);
}
