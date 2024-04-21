package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * This class represents a transfer in the banking system.
 * It contains the id, currency, status, reference number,
 * date and time, description, amount, sender card, and receiver card.
 */
@Setter
@Getter
@ToString
@Entity
@Table(name = "transfer")
public class Transfer implements Serializable {

    /**
     * The id of the transfer.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * The currency of the transfer.
     */
    @Enumerated(EnumType.STRING)
    private Currency currency;

    /**
     * The status of the transfer.
     */
    @Enumerated(EnumType.STRING)
    private FinancialStatus status;

    /**
     * The reference number of the transfer.
     */
    @Column(name = "reference_number", nullable = false)
    private String referenceNumber;

    /**
     * The date and time of the transfer.
     */
    @Column(name = "date_time", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm", iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime dateTime;

    /**
     * The description of the transfer.
     */
    @Column(name = "description", nullable = false)
    private String description;

    /**
     * The amount of the transfer.
     */
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    /**
     * The card used to send the transfer.
     */
    @JsonIgnore
    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "sender_card", nullable = false)
    private Card senderCard;

    /**
     * The card used to receive the transfer.
     */
    @JsonIgnore
    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "receiver_card", nullable = false)
    private Card receiverCard;
}
