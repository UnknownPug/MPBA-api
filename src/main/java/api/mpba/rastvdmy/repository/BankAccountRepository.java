package api.mpba.rastvdmy.repository;

import api.mpba.rastvdmy.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {

    Optional<List<BankAccount>> findAllByBankIdentityId(UUID bankIdentityId);

    @Query("SELECT a FROM BankAccount a WHERE a.bankIdentity.id IN :bankIdentitiesIds")
    List<BankAccount> findAllByBankIdentitiesId(List<UUID> bankIdentitiesIds);
}
