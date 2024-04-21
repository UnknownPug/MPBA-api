package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * This class represents a message in the system.
 * It contains the id, content, timestamp, sender, and receiver.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "message")
public class Message implements Serializable {

    /**
     * The id of the message.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * The content of the message.
     */
    @Column(name = "content", nullable = false)
    private String content;

    /**
     * The timestamp of when the message was sent.
     */
    @Column(name = "timestamp", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm", iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime timestamp;

    /**
     * The sender of the message.
     */
    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * The receiver of the message.
     */
    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;
}
