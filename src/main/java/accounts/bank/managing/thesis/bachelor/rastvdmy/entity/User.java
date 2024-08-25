package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.enums.UserRole;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * This class represents a user in the system.
 * It contains the id, user role, status, visibility, name, surname, date of birth, country of origin,
 * email, password, avatar, phone number, currency data, bank loan, sender messages, receiver messages, and cards.
 */
@Setter
@Getter
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_profile")
public class User implements Serializable {

    /**
     * The id of the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * The role of the user.
     */
    @Enumerated(EnumType.STRING)
    private UserRole role;

    /**
     * The status of the user.
     */
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    /**
     * The name of the user.
     */
    @NotBlank(message = "Name is mandatory")
    @Min(value = 2, message = "Name should have at least 2 characters")
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * The surname of the user.
     */
    @NotBlank(message = "Surname is mandatory")
    @Size(min = 2, max = 15, message = "Surname should have at least 2 characters and at most 15 characters")
    @Column(name = "surname", nullable = false)
    private String surname;

    /**
     * The date of birth of the user.
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    /**
     * The country of origin of the user.
     */
    @Column(name = "country_of_origin", nullable = false)
    private String countryOrigin;

    /**
     * The email of the user.
     */
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false)
    private String email;

    /**
     * The password of the user.
     */
    @NotBlank(message = "Password is mandatory")
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * The avatar of the user.
     */
    @Column(name = "avatar", nullable = false)
    private String avatar;

    /**
     * The phone number of the user.
     */
    @NotBlank(message = "Phone number is mandatory")
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    /**
     * The currency data associated with the user.
     */
    @ManyToMany
    @JsonIgnore
    @ToString.Exclude
    private List<CurrencyData> currencyData;

    /**
     * The messages sent by the user.
     */
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private List<Message> senderMessages;

    /**
     * The messages received by the user.
     */
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private List<Message> receiverMessages;

    /**
     * The access tokens associated with the user.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<AccessToken> accessTokens;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<BankIdentity> bankIdentities;

    /**
     * Encodes the password of the user.
     *
     * @param encoder The encoder to use for encoding the password.
     */
    public void encodePassword(PasswordEncoder encoder) {
        this.password = encoder.encode(password);
    }
}
