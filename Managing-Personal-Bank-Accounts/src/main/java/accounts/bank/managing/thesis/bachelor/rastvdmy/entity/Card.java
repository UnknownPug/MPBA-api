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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * This class represents a bank card.
 * It contains the id, status, card type, currency type, card number,
 * account number, iban, cvv, pin, holder name, swift, balance,
 * card expiration date, recipient time, user, card loan, deposit transaction,
 * sender transfer transaction, and receiver transfer transaction.
 */
@Entity
@Setter
@Getter
@ToString
@Table(name = "card")
public class Card implements Serializable {

    /**
     * The id of the card.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * The status of the card.
     */
    @Enumerated(EnumType.STRING)
    private CardStatus status;

    /**
     * The type of the card.
     */
    @Enumerated(EnumType.STRING)
    private CardType cardType;

    /**
     * The currency type of the card.
     */
    @Enumerated(EnumType.STRING)
    private Currency currencyType;

    /**
     * The card number.
     */
    @Column(name = "card_number", nullable = false)
    private String cardNumber;

    /**
     * The account number associated with the card.
     */
    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    /**
     * The IBAN of the card.
     */
    @Column(name = "iban", nullable = false)
    private String iban;

    /**
     * The CVV of the card.
     */
    @Column(name = "cvv", nullable = false)
    private Integer cvv;

    /**
     * The PIN of the card.
     */
    @Column(name = "pin", nullable = false)
    private Integer pin;

    /**
     * The name of the cardholder.
     */
    @Column(name = "card_holder", nullable = false)
    private String holderName;

    /**
     * The SWIFT code of the card.
     */
    @Column(name = "swift", nullable = false)
    private String swift;

    /**
     * The balance of the card.
     */
    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    /**
     * The expiration date of the card.
     */
    @Column(name = "expiration_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate cardExpirationDate;

    /**
     * The recipient time of the card.
     */
    @Column(name = "recipient_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm", iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime recipientTime;

    /**
     * The user associated with the card.
     */
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The bank loan associated with the card.
     */
    @JsonIgnore
    @OneToOne(mappedBy = "cardLoan", cascade = CascadeType.ALL)
    @ToString.Exclude
    private BankLoan cardLoan;

    /**
     * The deposit transaction associated with the card.
     */
    @JsonIgnore
    @OneToOne(mappedBy = "cardDeposit", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Deposit depositTransaction;

    /**
     * The list of sender transfer transactions associated with the card.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "senderCard", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Transfer> senderTransferTransaction;

    /**
     * The list of receiver transfer transactions associated with the card.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "receiverCard", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Transfer> receiverTransferTransaction;

    /**
     * Default constructor for the Card class.
     * Sets the status of the card to default.
     */
    public Card() {
        this.status = CardStatus.STATUS_CARD_DEFAULT;
    }
}
