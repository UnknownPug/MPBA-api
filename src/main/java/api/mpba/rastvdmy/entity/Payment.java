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
     * This serves as the primary key and uniquely identifies the payment transaction.
     */
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * The currency of the payment.
     * This field specifies the currency in which the payment is made.
     */
    @Enumerated(EnumType.STRING)
    private Currency currency;

    /**
     * The status of the payment.
     * This indicates the current state of the payment (e.g., PENDING, COMPLETED, FAILED).
     */
    @Enumerated(EnumType.STRING)
    private FinancialStatus status;

    /**
     * The type of the payment.
     * This specifies the nature of the payment (e.g., TRANSFER, PAYMENT).
     */
    @Enumerated(EnumType.STRING)
    private PaymentType type;

    /**
     * The name of the sender of the payment.
     * This field stores the full name of the person or entity sending the payment.
     */
    @Column(name = "sender_name", nullable = false)
    private String senderName;

    /**
     * The name of the recipient of the payment.
     * This field stores the full name of the person or entity receiving the payment.
     */
    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    /**
     * The date and time of the payment.
     * This field records when the payment was initiated and is required.
     */
    @Column(name = "date_time", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateTime;

    /**
     * A brief description of the payment.
     * This field provides additional details about the payment and can contain a maximum of 255 characters.
     */
    @Size(max = 255, message = "Description can contain a maximum of 255 characters")
    @Column(name = "description")
    private String description;

    /**
     * The amount of money being transferred in the payment.
     * This field must be positive and indicates the total payment amount.
     */
    @Positive(message = "Amount should be positive")
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    /**
     * The bank account from which the payment is sent.
     * This establishes a many-to-one relationship with the BankAccount entity,
     * indicating the source of funds for the payment.
     */
    @ManyToOne
    @JsonIgnore
    @ToString.Exclude
    @JoinColumn(name = "sender_account_id")
    private BankAccount senderAccount;

    /**
     * The bank account to which the payment is sent.
     * This establishes a many-to-one relationship with the BankAccount entity,
     * indicating the destination for the funds.
     */
    @ManyToOne
    @JsonIgnore
    @ToString.Exclude
    @JoinColumn(name = "recipient_account_id")
    private BankAccount recipientAccount;

    /**
     * The card used to send the payment.
     * This establishes a many-to-one relationship with the Card entity,
     * indicating which card was used for the transaction.
     */
    @ManyToOne
    @JsonIgnore
    @ToString.Exclude
    @JoinColumn(name = "sender_card_id")
    private Card senderCard;
}
