package accounts.bank.managing.thesis.bachelor.rastvdmy.repository;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.BankIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BankIdentityRepository extends JpaRepository<BankIdentity, String> {
    BankIdentity findByBankName(String bankName);

    List<BankIdentity> findAllByUserId(UUID id);
}
