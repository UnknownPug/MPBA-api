package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.enums.Currency;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import javax.validation.constraints.NotBlank;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * The currency of the Bank Account.
     */
    @Enumerated(EnumType.STRING)
    private Currency currency;

    /**
     * The balance of the Bank Account.
     */
    @NotBlank
    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    /**
     * The account number associated with the Bank Account.
     */
    @NotBlank
    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    /**
     * The SWIFT code of the Bank Account.
     */
    @NotBlank
    @Column(name = "swift", nullable = false)
    private String swift;

    /**
     * The IBAN of the Bank Account.
     */
    @NotBlank
    @Column(name = "iban", nullable = false)
    private String iban;

    /**
     * The cards associated with the Bank Account.
     */
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<Card> cards;

    /**
     * The bank identity of the Bank Account.
     */
    @ManyToOne
    @JsonIgnore
    @ToString.Exclude
    @JoinColumn(name = "bank_id", nullable = false)
    private BankIdentity bankIdentity;

    /**
     * The payments received by the Bank Account.
     */
    @OneToMany(mappedBy = "senderAccount")
    @JsonIgnore
    @ToString.Exclude
    private List<Payment> paymentsSender;

    /**
     * The payments sent by the Bank Account.
     */
    @OneToMany(mappedBy = "recipientAccount")
    @JsonIgnore
    @ToString.Exclude
    private List<Payment> paymentsRecipient;

}
