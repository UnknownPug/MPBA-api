package api.mpba.rastvdmy.repository;

import api.mpba.rastvdmy.entity.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for managing access tokens in the database.
 * It extends JpaRepository, which provides basic CRUD operations and JPA-related methods.
 */
@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, UUID> {}