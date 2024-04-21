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

/**
 * This class represents a bank loan.
 * It contains the id, currency, loan amount, repaid loan, start date,
 * expiration date, reference number, card loan, and user loan.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "bank_loan")
public class BankLoan implements Serializable {

    /**
     * The id of the bank loan.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * The currency of the bank loan.
     */
    @Enumerated(EnumType.STRING)
    private Currency currency;

    /**
     * The loan amount of the bank loan.
     */
    @Column(name = "loan_amount", nullable = false)
    private BigDecimal loanAmount;

    /**
     * The repaid loan amount of the bank loan.
     */
    @Column(name = "repaid_loan", nullable = false)
    private BigDecimal repaidLoan;

    /**
     * The start date of the bank loan.
     */
    @Column(name = "start_time", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * The expiration date of the bank loan.
     */
    @Column(name = "expiration_date", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate expirationDate;

    /**
     * The reference number of the bank loan.
     */
    @Column(name = "reference_number", nullable = false)
    private String referenceNumber;

    /**
     * The card associated with the bank loan.
     */
    @JsonIgnore
    @OneToOne
    @ToString.Exclude
    @JoinTable(name = "card_loan",
            joinColumns = @JoinColumn(name = "loan_id"),
            inverseJoinColumns = @JoinColumn(name = "card_id"))
    private Card cardLoan;

    /**
     * The user associated with the bank loan.
     */
    @JsonIgnore
    @OneToOne
    @ToString.Exclude
    @JoinTable(name = "user_loan",
            joinColumns = @JoinColumn(name = "loan_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private User userLoan;
}
