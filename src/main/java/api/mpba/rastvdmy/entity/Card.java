package api.mpba.rastvdmy.entity;

import api.mpba.rastvdmy.entity.enums.CardCategory;
import api.mpba.rastvdmy.entity.enums.CardStatus;
import api.mpba.rastvdmy.entity.enums.CardType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * This class represents a bank card.
 * It contains the id, status, card type, currency type, card number,
 * account number, iban, cvv, pin, holder name, swift, balance,
 * card expiration date, recipient time, user, card loan, deposit transaction,
 * sender transfer transaction, and receiver transfer transaction.
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
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * The category of the card.
     */
    @Enumerated(EnumType.STRING)
    private CardCategory category;

    /**
     * The type of the card.
     */
    @Enumerated(EnumType.STRING)
    private CardType type;

    /**
     * The status of the card.
     */
    @Enumerated(EnumType.STRING)
    private CardStatus status;

    /**
     * The card number.
     */
    @NotBlank
    @Column(name = "card_number", nullable = false)
    private String cardNumber;

    /**
     * The CVV of the card.
     */
    @NotBlank
    @Column(name = "cvv", nullable = false)
    private String cvv;

    /**
     * The PIN of the card.
     */
    @NotBlank
    @Column(name = "pin", nullable = false)
    private String pin;

    /**
     * The start date of the card.
     */
    @NotBlank
    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * The expiration date of the card.
     */
    @NotBlank
    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    /**
     * The balance of the card.
     */
    @ManyToOne
    @JsonIgnore
    @ToString.Exclude
    @JoinColumn(name = "account_id")
    private BankAccount account;

    /**
     * The sender payments of the card.
     */
    @OneToMany(mappedBy = "senderCard")
    @JsonIgnore
    @ToString.Exclude
    private List<Payment> senderPayments;
}
