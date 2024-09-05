package api.mpba.rastvdmy.repository;

import api.mpba.rastvdmy.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * This interface represents the repository for the Message entity.
 * It extends JpaRepository to provide methods to manipulate Message entities.
 * JpaRepository is a JPA specific extension of Repository
 * that provides JPA related methods such as flushing the persistence context and deleting records in a batch.
 * It is annotated with @Repository to indicate that it's a component that directly accesses the database.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    List<Message> findAllBySenderIdAndReceiverName(UUID id, String username);

    Message findBySenderIdAndContent(UUID id, String content);
}
