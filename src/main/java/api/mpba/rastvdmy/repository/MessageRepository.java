package api.mpba.rastvdmy.repository;

import api.mpba.rastvdmy.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * This interface represents the repository for the Message entity.
 * It extends JpaRepository to provide methods for manipulating Message entities.
 * JpaRepository is a JPA-specific extension of Repository
 * that provides methods for common database operations, such as saving, deleting, and finding records.
 * It is annotated with @Repository to indicate that it serves as a data access layer.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, String> {}
