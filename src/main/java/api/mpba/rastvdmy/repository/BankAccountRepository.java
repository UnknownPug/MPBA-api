package api.mpba.rastvdmy.repository;

import api.mpba.rastvdmy.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing bank accounts in the database.
 * It extends JpaRepository, which provides basic CRUD operations and JPA-related methods.
 */
@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {

    /**
     * Finds all bank accounts associated with a specific bank identity ID.
     *
     * @param bankIdentityId The ID of the bank identity.
     * @return An optional list of bank accounts linked to the provided bank identity ID.
     */
    Optional<List<BankAccount>> findAllByBankIdentityId(UUID bankIdentityId);

    /**
     * Finds all bank accounts associated with a list of bank identity IDs.
     *
     * @param bankIdentitiesIds The list of bank identity IDs.
     * @return A list of bank accounts linked to the provided bank identity IDs.
     */
    @Query("SELECT a FROM BankAccount a WHERE a.bankIdentity.id IN :bankIdentitiesIds")
    List<BankAccount> findAllByBankIdentitiesId(List<UUID> bankIdentitiesIds);
}
