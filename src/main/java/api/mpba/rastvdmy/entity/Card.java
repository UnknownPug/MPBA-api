package api.mpba.rastvdmy.entity;

import api.mpba.rastvdmy.entity.enums.CardCategory;
import api.mpba.rastvdmy.entity.enums.CardStatus;
import api.mpba.rastvdmy.entity.enums.CardType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * This class represents a bank card.
 * It contains the id, status, card type, currency type, card number,
 * account number, CVV, PIN, holder name, SWIFT code, balance,
 * card expiration date, user, sender payments, and related transactions.
 */
@Setter
@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "card")
public class Card implements Serializable {

    /**
     * The id of the card.
     * This serves as the primary key and uniquely identifies the card.
     */
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * The category of the card, indicating its type (e.g., debit, credit).
     * This field uses an enumerated type to define the card's category.
     */
    @Enumerated(EnumType.STRING)
    private CardCategory category;

    /**
     * The type of the card (e.g., VISA, MASTERCARD).
     * This field uses an enumerated type to specify the card type.
     */
    @Enumerated(EnumType.STRING)
    private CardType type;

    /**
     * The status of the card (e.g., active, blocked).
     * This field uses an enumerated type to define the card's status.
     */
    @Enumerated(EnumType.STRING)
    private CardStatus status;

    /**
     * The card number.
     * This field is mandatory and should be provided as a valid card number.
     */
    @Column(name = "card_number", nullable = false)
    private String cardNumber;

    /**
     * The CVV (Card Verification Value) of the card.
     * This field is mandatory and should contain the card's CVV code.
     */
    @Column(name = "cvv", nullable = false)
    private String cvv;

    /**
     * The PIN (Personal Identification Number) for the card.
     * This field is mandatory and should be kept secure.
     */
    @Column(name = "pin", nullable = false)
    private String pin;

    /**
     * The start date of the card's validity.
     * This field indicates when the card becomes active and valid for use.
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * The expiration date of the card.
     * This field indicates when the card is no longer valid for transactions.
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    /**
     * The bank account associated with the card.
     * This field establishes a many-to-one relationship with the BankAccount entity.
     */
    @ManyToOne
    @JsonIgnore
    @ToString.Exclude
    @JoinColumn(name = "account_id")
    private BankAccount account;

    /**
     * The list of payments made with this card as the sender.
     * This field establishes a one-to-many relationship with the Payment entity.
     */
    @OneToMany(mappedBy = "senderCard")
    @JsonIgnore
    @ToString.Exclude
    private List<Payment> senderPayments;
}
