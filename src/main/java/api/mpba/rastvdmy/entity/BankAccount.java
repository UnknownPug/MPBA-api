package api.mpba.rastvdmy.entity;

import api.mpba.rastvdmy.entity.enums.Currency;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * This class represents a bank account in the banking system.
 * It contains the id, currency, balance, account number, IBAN,
 * associated cards, bank identity, payments sent, and payments received.
 */
@Getter
@Setter
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bank_account")
public class BankAccount implements Serializable {

    /**
     * The id of the bank account.
     * This serves as the primary key and uniquely identifies the bank account.
     */
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * The currency of the bank account.
     * This is represented as an enum, allowing for easy identification of currency types.
     */
    @Enumerated(EnumType.STRING)
    private Currency currency;

    /**
     * The balance of the bank account.
     * This should always be positive; validated with a constraint.
     */
    @Positive(message = "Balance should be positive")
    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    /**
     * The account number of the bank account.
     * This field is mandatory and should be provided as a valid account number.
     */
    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    /**
     * The IBAN (International Bank Account Number) of the bank account.
     * This field is mandatory and should be provided as a valid IBAN.
     */
    @Column(name = "iban", nullable = false)
    private String iban;

    /**
     * List of cards associated with the bank account.
     * This establishes a one-to-many relationship with the Card entity,
     * allowing for multiple cards to be linked to a single bank account.
     */
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    private List<Card> cards;

    /**
     * The bank identity associated with the bank account.
     * This establishes a many-to-one relationship with the BankIdentity entity,
     * indicating that a bank account belongs to a specific bank identity.
     */
    @ManyToOne
    @JsonIgnore
    @ToString.Exclude
    @JoinColumn(name = "bank_id", nullable = false)
    private BankIdentity bankIdentity;

    /**
     * Payments sent from the bank account.
     * This establishes a one-to-many relationship with the Payment entity,
     * indicating all payments initiated from this bank account.
     */
    @OneToMany(mappedBy = "senderAccount")
    @JsonIgnore
    @ToString.Exclude
    private List<Payment> paymentsSender;

    /**
     * Payments received by the bank account.
     * This establishes a one-to-many relationship with the Payment entity,
     * indicating all payments received into this bank account.
     */
    @OneToMany(mappedBy = "recipientAccount")
    @JsonIgnore
    @ToString.Exclude
    private List<Payment> paymentsRecipient;
}
