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
 * This class represents a deposit in the bank.
 * It contains the id, currency, start date, expiration date, description, deposit amount,
 * deposit card, reference number, and the card associated with the deposit.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "deposit")
public class Deposit implements Serializable {

    /**
     * The id of the deposit.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * The currency of the deposit.
     */
    @Enumerated(EnumType.STRING)
    private Currency currency;

    /**
     * The start date of the deposit.
     */
    @Column(name = "start_date", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm", iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startDate;

    /**
     * The expiration date of the deposit.
     */
    @Column(name = "expiration_date", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm", iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime expirationDate;

    /**
     * The description of the deposit.
     */
    @Column(name = "description", nullable = false)
    private String description;

    /**
     * The amount of the deposit.
     */
    @Column(name = "deposit_amount", nullable = false)
    private BigDecimal depositAmount;

    /**
     * The card used for the deposit.
     */
    @Column(name = "deposit_card")
    private String depositCard;

    /**
     * The reference number of the deposit.
     */
    @Column(name = "reference_number", nullable = false)
    private String referenceNumber;

    /**
     * The card associated with the deposit.
     */
    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "card_deposit", nullable = false)
    @ToString.Exclude
    private Card cardDeposit;
}
