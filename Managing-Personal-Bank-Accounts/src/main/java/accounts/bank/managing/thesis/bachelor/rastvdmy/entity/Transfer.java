package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@Entity
@Table(name = "transfer")
public class Transfer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    private FinancialStatus status;

    @Column(name = "reference_number", nullable = false)
    @Size(min = 1, max = 11)
    private String referenceNumber;

    @Column(name = "date_time", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm", iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime dateTime;

    @Column(name = "description", nullable = false)
    @Size(min = 1, max = 100, message = "Description must be between 1 and 100 characters")
    private String description;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @JsonIgnore
    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "sender_card", nullable = false)
    private Card senderCard;

    @JsonIgnore
    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "receiver_card", nullable = false)
    private Card receiverCard;
}
