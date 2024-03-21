package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@ToString
@Table(name = "card")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    @Enumerated(EnumType.STRING)
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    private Currency currencyType;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "card_number", nullable = false, unique = true)
    @Size(min = 16, max = 16, message = "Card number must be 16 digits.")
    private String cardNumber;

    @Column(name = "cvv", nullable = false)
    private Integer cvv;

    @Column(name = "pin", nullable = false)
    private Integer pin;

    @Column(name = "card_holder", nullable = false)
    private String holderName;

    @Column(name = "account_number", nullable = false, unique = true)
    @Pattern(regexp = "^\\d{10}/\\d{4}$")
    private String accountNumber;

    @Column(name = "iban", nullable = false, unique = true)
    @Size(min = 24, max = 24)
    private String iban;

    @Column(name = "swift", nullable = false)
    @Size(min = 8, max = 8)
    private String swift;

    @Column(name = "expiration_date", nullable = false)
    @DateTimeFormat(pattern = "dd.MM.yyyy", iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDateTime cardExpirationDate;

    @Column(name = "recipient_time", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm", iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime recipientTime;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @OneToOne(mappedBy = "cardLoan", cascade = CascadeType.ALL)
    @ToString.Exclude
    private BankLoan cardLoan;

    @JsonIgnore
    @OneToOne(mappedBy = "cardDeposit", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Deposit depositTransaction;

    @JsonIgnore
    @OneToOne(mappedBy = "senderCard", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Transfer senderTransferTransaction;

    @JsonIgnore
    @OneToOne(mappedBy = "receiverCard", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Transfer receiverTransferTransaction;

    @JsonIgnore
    @OneToOne(mappedBy = "card", cascade = CascadeType.ALL)
    @ToString.Exclude
    private CardConversion conversionTransaction;
}
