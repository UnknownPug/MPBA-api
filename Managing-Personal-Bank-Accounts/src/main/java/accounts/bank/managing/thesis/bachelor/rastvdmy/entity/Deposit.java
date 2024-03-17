package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@Table(name = "deposit")
public class Deposit {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(name = "start_date", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm", iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startDate;

    @Column(name = "expiration_date", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm", iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime expirationDate;

    @Column(name = "description", nullable = false)
    @Size(min = 1, max = 100, message = "The length of the description must be between 1 and 100 characters")
    private String description;

    @Column(name = "reference_number", nullable = false, unique = true)
    @Size(min = 1, max = 11)
    private String referenceNumber;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "card_deposit", nullable = false)
    @ToString.Exclude
    private Card cardDeposit;
}
