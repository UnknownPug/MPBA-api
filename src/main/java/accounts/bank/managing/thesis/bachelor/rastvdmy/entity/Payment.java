package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.enums.Currency;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.enums.FinancialStatus;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.enums.PaymentType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
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
@Table(name = "payment")
public class Payment implements Serializable {

    /**
     * The id of the payment.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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

    /**
     * The sender name of the payment.
     */
    @NotBlank
    @Column(name = "sender_name", nullable = false)
    private String senderName;

    /**
     * The recipient name of the payment.
     */
    @NotBlank
    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    /**
     * The date and time of the payment.
     */
    @NotBlank
    @Column(name = "date_time", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateTime;

    /**
     * The description of the payment.
     */
    @Column(name = "description")
    private String description;

    /**
     * The amount of the payment.
     */
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    /**
     * The sender account of the payment.
     */
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
