package api.mpba.rastvdmy.repository;

import api.mpba.rastvdmy.entity.BankIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing bank identities in the database.
 * It extends JpaRepository, providing basic CRUD operations and JPA-related methods.
 */
@Repository
public interface BankIdentityRepository extends JpaRepository<BankIdentity, UUID> {

    /**
     * Finds all bank identities associated with a specific user profile ID.
     *
     * @param id The ID of the user profile.
     * @return An optional list of bank identities linked to the provided user profile ID.
     */
    Optional<List<BankIdentity>> findAllByUserProfileId(UUID id);

    /**
     * Finds a bank identity by its name, connected to a specific user ID.
     *
     * @param bankName The name of the bank.
     * @param userId   The ID of the user profile.
     * @return An optional bank identity matching the provided bank name and user ID.
     */
    @Query("SELECT b FROM BankIdentity b JOIN b.userProfile u WHERE b.bankName = :bankName AND u.id = :userId")
    Optional<BankIdentity> findByNameAndConnectedToUserId(String bankName, UUID userId);

    /**
     * Finds a bank identity by user ID and bank name, fetching associated bank accounts.
     *
     * @param userId   The ID of the user profile.
     * @param bankName The name of the bank.
     * @return An optional bank identity linked to the provided user ID and bank name,
     * including its associated bank accounts.
     */
    @Query("SELECT b FROM BankIdentity b LEFT JOIN FETCH b.bankAccounts " +
            "WHERE b.userProfile.id = :userId AND b.bankName = :bankName")
    Optional<BankIdentity> findByUserIdAndBankNameWithAccounts(@Param("userId") UUID userId,
                                                               @Param("bankName") String bankName);

    /**
     * Finds a bank identity by user profile ID and bank name.
     *
     * @param userId   The ID of the user profile.
     * @param bankName The name of the bank.
     * @return An optional bank identity matching the provided user ID and bank name.
     */
    Optional<BankIdentity> findByUserProfileIdAndBankName(UUID userId, String bankName);
}
