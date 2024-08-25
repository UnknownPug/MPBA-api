package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * This class represents an access token in the banking system.
 * It contains the id, token, expiration date, user, and bank identity.
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "access_token")
public class AccessToken implements Serializable {

    /**
     * The id of the access token.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * The token of the access token.
     */
    @Column(name = "token", nullable = false)
    private String token;

    /**
     * The expiration date of the access token.
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    /**
     * The user of the access token.
     */
    @ManyToOne
    @JsonIgnore
    @ToString.Exclude
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The bank identity of the access token.
     */
    @ManyToOne
    @JsonIgnore
    @ToString.Exclude
    @JoinColumn(name = "bank_id", nullable = false)
    private BankIdentity bankIdentity;
}
