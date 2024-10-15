package api.mpba.rastvdmy.repository;

import api.mpba.rastvdmy.entity.BankIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankIdentityRepository extends JpaRepository<BankIdentity, UUID> {

    Optional<List<BankIdentity>> findAllByUserProfileId(UUID id);

    @Query("SELECT b FROM BankIdentity b JOIN b.userProfile u WHERE b.bankName = :bankName AND u.id = :userId")
    Optional<BankIdentity> findByNameAndConnectedToUserId(String bankName, UUID userId);

    @Query("SELECT b FROM BankIdentity b LEFT JOIN FETCH b.bankAccounts WHERE b.userProfile.id = :userId AND b.bankName = :bankName")
    Optional<BankIdentity> findByUserIdAndBankNameWithAccounts(@Param("userId") UUID userId, @Param("bankName") String bankName);

    Optional<BankIdentity> findByUserProfileIdAndBankName(UUID userId, String bankName);
}
