package api.mpba.rastvdmy.repository;

import api.mpba.rastvdmy.entity.CurrencyData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * This interface represents the repository for the CurrencyData entity.
 * It extends JpaRepository to provide methods to manipulate CurrencyData entities.
 * JpaRepository is a JPA-specific extension of Repository
 * that provides JPA-related methods such as flushing the persistence context and deleting records in a batch.
 * It is annotated with @Repository to indicate that it's a component that directly accesses the database.
 */
@Repository
public interface CurrencyDataRepository extends JpaRepository<CurrencyData, UUID> {

    /**
     * Finds a CurrencyData entity by its currency type.
     *
     * @param currency The currency type of the CurrencyData entity to find.
     * @return The CurrencyData entity with the given currency type, or null if no such entity exists.
     */
    CurrencyData findByCurrency(String currency);

    /**
     * Finds all CurrencyData entities by their currency type.
     *
     * @param currencyType The currency type of the CurrencyData entities to find.
     * @return A list of CurrencyData entities with the given currency type,
     *         or an empty list if no such entities exist.
     */
    List<CurrencyData> findAllByCurrency(String currencyType);
}
