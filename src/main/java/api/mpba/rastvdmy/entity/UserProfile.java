package api.mpba.rastvdmy.entity;

import api.mpba.rastvdmy.entity.enums.UserRole;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * This class represents a user profile in the banking system.
 * It implements UserDetails for Spring Security and contains
 * user-specific information such as role, status, personal details,
 * contact information, and relationships to other entities.
 */
@Setter
@Getter
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_profile")
public class UserProfile implements UserDetails, Serializable {

    /**
     * The unique identifier for the user profile.
     * This serves as the primary key for the user profile table.
     */
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * The role of the user.
     * This field defines the user's permissions in the system, e.g., ADMIN, USER.
     */
    @Enumerated(EnumType.STRING)
    private UserRole role;

    /**
     * The status of the user.
     * This indicates whether the user is active, inactive, etc.
     */
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    /**
     * The first name of the user.
     * This field is mandatory and should have a minimum of 2 characters.
     */
    @Column(name = "name", nullable = false)
    @Size(min = 2, message = "Name should have at least 2 characters")
    private String name;

    /**
     * The surname of the user.
     * This field is mandatory and should have a minimum of 2 characters.
     */
    @Column(name = "surname", nullable = false)
    @Size(min = 2, message = "Surname should have at least 2 characters")
    private String surname;

    /**
     * The date of birth of the user.
     * This field is mandatory and should be formatted as a date string.
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "date_of_birth", nullable = false)
    private String dateOfBirth;

    /**
     * The country of origin of the user.
     * This field is mandatory and stores the user's country.
     */
    @Column(name = "country_of_origin", nullable = false)
    private String countryOfOrigin;

    /**
     * The email address of the user.
     * This field is mandatory, unique, and should be a valid email format.
     */
    @Email(message = "Email should be valid")
    @Column(unique = true, name = "email", nullable = false)
    private String email;

    /**
     * The password of the user.
     * This field is mandatory and stores the user's password.
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * The avatar of the user.
     * This field is mandatory and stores the URL or path of the user's avatar image.
     */
    @Column(name = "avatar", nullable = false)
    private String avatar;

    /**
     * The phone number of the user.
     * This field is mandatory and stores the user's contact phone number.
     */
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    /**
     * The currencies associated with the user.
     * This is a many-to-many relationship with the CurrencyData entity.
     */
    @ManyToMany
    @JsonIgnore
    @ToString.Exclude
    private List<CurrencyData> currencyData;

    /**
     * The messages sent by the user.
     * This is a one-to-many relationship with the Message entity,
     * where the user is the sender of the messages.
     */
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<Message> senderMessages;

    /**
     * The messages received by the user.
     * This is a one-to-many relationship with the Message entity,
     * where the user is the receiver of the messages.
     */
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<Message> receiverMessages;

    /**
     * The access tokens associated with the user.
     * This is a one-to-many relationship with the AccessToken entity,
     * indicating the tokens issued for authentication.
     */
    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<AccessToken> accessTokens;

    /**
     * The bank identities associated with the user.
     * This is a one-to-many relationship with the BankIdentity entity,
     * representing the user's linked bank accounts and identities.
     */
    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    private List<BankIdentity> bankIdentities;

    /**
     * Returns the authorities granted to the user.
     * This method is required for Spring Security to manage user roles and permissions.
     *
     * @return a collection of granted authorities.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * Returns the username used for authentication.
     * In this implementation, the email address serves as the username.
     *
     * @return the email address of the user.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Returns the password used for authentication.
     *
     * @return the password of the user.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Indicates whether the user's account is non-expired.
     * In this implementation, the account is always considered non-expired.
     *
     * @return true if the account is non-expired, false otherwise.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user's account is non-locked.
     * In this implementation, the account is always considered non-locked.
     *
     * @return true if the account is non-locked, false otherwise.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials are non-expired.
     * In this implementation, the credentials are always considered non-expired.
     *
     * @return true if the credentials are non-expired, false otherwise.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user's account is enabled.
     * In this implementation, the account is always considered enabled.
     *
     * @return true if the account is enabled, false otherwise.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}