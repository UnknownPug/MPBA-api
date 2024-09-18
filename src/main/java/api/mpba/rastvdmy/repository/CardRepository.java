package api.mpba.rastvdmy.repository;

import api.mpba.rastvdmy.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {
    Optional<List<Card>> findAllByAccountId(UUID id);

    Optional<Card> findByAccountId(UUID id);
}
