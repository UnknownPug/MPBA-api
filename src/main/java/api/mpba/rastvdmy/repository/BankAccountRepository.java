package api.mpba.rastvdmy.repository;

import api.mpba.rastvdmy.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {

    List<BankAccount> findAllByBankIdentityId(UUID bankIdentityId);

    BankAccount findByBankIdentityIdAndId(UUID bankIdentity, UUID id);

    Optional<BankAccount> findByBankIdentityId(UUID id);

    // FIXME: account number will be encoded. Use Account id instead!
    Optional<BankAccount> findByAccountNumber(String accountNumber);
}
