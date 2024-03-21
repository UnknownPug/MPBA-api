package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@Table(name = "bank_loan")
public class BankLoan {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(name = "loan_amount", nullable = false)
    private BigDecimal loanAmount;

    @Column(name = "repaid_loan", nullable = false)
    private BigDecimal repaidLoan;

    @Column(name = "start_time", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime startDate;

    @Column(name = "expiration_date", nullable = false)
    @DateTimeFormat(pattern = "dd.MM.yyyy", iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDateTime expirationDate;

    @Column(name = "reference_number", nullable = false, unique = true)
    @Size(min = 1, max = 11)
    private String referenceNumber;

    @JsonIgnore
    @OneToOne
    @ToString.Exclude
    @JoinColumn(name = "card", nullable = false)
    private Card cardLoan;

    @JsonIgnore
    @OneToOne
    @ToString.Exclude
    @JoinColumn(name = "user_id", nullable = false)
    private User userLoan;
}
