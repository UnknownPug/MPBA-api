package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a user in the system.
 * It contains the id, user role, status, visibility, name, surname, date of birth, country of origin,
 * email, password, avatar, phone number, currency data, bank loan, sender messages, receiver messages, and cards.
 */
@Setter
@Getter
@ToString
@Entity
@Table(name = "user_profile")
public class User implements Serializable {

    /**
     * The id of the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * The role of the user.
     */
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    /**
     * The status of the user.
     */
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    /**
     * The visibility of the user.
     */
    @Enumerated(EnumType.STRING)
    private UserVisibility visibility;

    /**
     * The name of the user.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * The surname of the user.
     */
    @Column(name = "surname", nullable = false)
    private String surname;

    /**
     * The date of birth of the user.
     */
    @Column(name = "date_of_birth", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    /**
     * The country of origin of the user.
     */
    @Column(name = "country_of_origin", nullable = false)
    private String countryOrigin;

    /**
     * The email of the user.
     */
    @Column(name = "email", nullable = false)
    private String email;

    /**
     * The password of the user.
     */
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
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    /**
     * The currency data associated with the user.
     */
    @JsonIgnore
    @ManyToMany
    @ToString.Exclude
    private List<CurrencyData> currencyData;

    /**
     * The bank loan associated with the user.
     */
    @JsonIgnore
    @OneToOne(mappedBy = "userLoan", cascade = CascadeType.ALL)
    @ToString.Exclude
    private BankLoan bankLoan;

    /**
     * The messages sent by the user.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Message> senderMessages;

    /**
     * The messages received by the user.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Message> receiverMessages;

    /**
     * The cards associated with the user.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Card> cards;

    /**
     * Default Constructor for user.
     * Sets the user role to ROLE_USER, visibility to STATUS_ONLINE,
     * status to STATUS_DEFAULT, and avatar to a default image.
     * Initializes the card list.
     */
    public User() {
        this.userRole = UserRole.ROLE_USER;
        this.visibility = UserVisibility.STATUS_ONLINE;
        this.status = UserStatus.STATUS_DEFAULT;
        this.avatar = "https://i0.wp.com/sbcf.fr/wp-content/uploads/2018/03/sbcf-default-avatar.png?ssl=1";
        this.cards = new ArrayList<>();
    }

    /**
     * Constructor for user with a specified role.
     * Sets the user role to the specified role and visibility to STATUS_ONLINE.
     *
     * @param role The role of the user.
     */
    public User(UserRole role) {
        this.userRole = role;
        this.visibility = UserVisibility.STATUS_ONLINE;
    }

    /**
     * Encodes the password of the user.
     *
     * @param encoder The encoder to use for encoding the password.
     */
    public void encodePassword(PasswordEncoder encoder) {
        this.password = encoder.encode(password);
    }

    /**
     * Erases the password of the user.
     */
    public void erasePassword() {
        this.password = null;
    }
}
