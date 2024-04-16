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

@Entity
@Setter
@Getter
@ToString
@Table(name = "card")
public class Card implements Serializable {

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

    @Column(name = "card_number", nullable = false)
    private String cardNumber;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "iban", nullable = false)
    private String iban;

    @Column(name = "cvv", nullable = false)
    private Integer cvv;

    @Column(name = "pin", nullable = false)
    private Integer pin;

    @Column(name = "card_holder", nullable = false)
    private String holderName;

    @Column(name = "swift", nullable = false)
    private String swift;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "expiration_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate cardExpirationDate;

    @Column(name = "recipient_time")
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
    @OneToMany(mappedBy = "senderCard", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Transfer> senderTransferTransaction;

    @JsonIgnore
    @OneToMany(mappedBy = "receiverCard", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Transfer> receiverTransferTransaction;

    public Card() {
        this.status = CardStatus.STATUS_CARD_DEFAULT;
    }
}
