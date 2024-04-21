package accounts.bank.managing.thesis.bachelor.rastvdmy.repository;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * This interface represents the repository for the Message entity.
 * It extends JpaRepository to provide methods to manipulate Message entities.
 * JpaRepository is a JPA specific extension of Repository
 * that provides JPA related methods such as flushing the persistence context and deleting records in a batch.
 * It is annotated with @Repository to indicate that it's a component that directly accesses the database.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Finds all Message entities by their content.
     *
     * @param content The content of the Message entities to find.
     * @return A list of Message entities with the given content, or an empty list if no such entities exist.
     */
    List<Message> findByContent(String content);
}
