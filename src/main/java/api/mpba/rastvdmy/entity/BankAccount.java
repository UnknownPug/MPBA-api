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
 * It contains the id, currency, balance, account number, SWIFT code,
 * IBAN, cards, bank identity, payments received, and payments sent.
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
     */
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Positive(message = "Balance should be positive")
    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "swift", nullable = false)
    private String swift;

    @Column(name = "iban", nullable = false)
    private String iban;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<Card> cards;

    @ManyToOne
    @JsonIgnore
    @ToString.Exclude
    @JoinColumn(name = "bank_id", nullable = false)
    private BankIdentity bankIdentity;

    @OneToMany(mappedBy = "senderAccount")
    @JsonIgnore
    @ToString.Exclude
    private List<Payment> paymentsSender;

    @OneToMany(mappedBy = "recipientAccount")
    @JsonIgnore
    @ToString.Exclude
    private List<Payment> paymentsRecipient;
}
