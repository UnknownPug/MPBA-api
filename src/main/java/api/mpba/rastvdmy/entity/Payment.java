package api.mpba.rastvdmy.entity;

import api.mpba.rastvdmy.entity.enums.Currency;
import api.mpba.rastvdmy.entity.enums.FinancialStatus;
import api.mpba.rastvdmy.entity.enums.PaymentType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * This class represents a payment in the banking system.
 * It contains the id, currency, status, reference number,
 * date and time, description, amount, sender card, and receiver card.
 */
@Setter
@Getter
@ToString
@Entity
@Builder
@Table(name = "payment")
@NoArgsConstructor
@AllArgsConstructor
public class Payment implements Serializable {

    /**
     * The id of the payment.
     */
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * The currency of the payment.
     */
    @Enumerated(EnumType.STRING)
    private Currency currency;

    /**
     * The status of the payment.
     */
    @Enumerated(EnumType.STRING)
    private FinancialStatus status;

    /**
     * The type of the payment.
     */
    @Enumerated(EnumType.STRING)
    private PaymentType type;

    @Column(name = "sender_name", nullable = false)
    private String senderName;

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "date_time", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateTime;

    @Size(max = 255, message = "Description can contain maximum 255 characters")
    @Column(name = "description")
    private String description;

    @Positive(message = "Amount should be positive")
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @ManyToOne
    @JsonIgnore
    @ToString.Exclude
    @JoinColumn(name = "sender_account_id")
    private BankAccount senderAccount;

    /**
     * The recipient account of the payment.
     */
    @ManyToOne
    @JsonIgnore
    @ToString.Exclude
    @JoinColumn(name = "recipient_account_id")
    private BankAccount recipientAccount;

    /**
     * The sender card of the payment.
     */
    @ManyToOne
    @JsonIgnore
    @ToString.Exclude
    @JoinColumn(name = "sender_card_id")
    private Card senderCard;
}
