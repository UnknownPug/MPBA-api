package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * This class represents a message in the system.
 * It contains the id, content, timestamp, sender, and receiver.
 */
@Getter
@Setter
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "message")
public class Message implements Serializable {

    /**
     * The id of the message.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * The content of the message.
     */
    @NotBlank(message = "Content is mandatory")
    @Size(min = 1, max = 255, message = "Message content must be between 1 and 255 characters")
    @Column(name = "content", nullable = false)
    private String content;

    /**
     * The timestamp of when the message was sent.
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm", iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * The sender of the message.
     */
    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "sender_id")
    private User sender;

    /**
     * The receiver of the message.
     */
    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "receiver_id")
    private User receiver;
}
