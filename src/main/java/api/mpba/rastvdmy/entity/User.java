package api.mpba.rastvdmy.entity;

import api.mpba.rastvdmy.entity.enums.UserRole;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_profile")
public class User implements UserDetails, Serializable {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(name = "name", nullable = false)
    @Size(min = 2, message = "Name should have at least 2 characters")
    @NotBlank(message = "Name is mandatory")
    private String name;

    @Column(name = "surname", nullable = false)
    @Size(min = 2, message = "Surname should have at least 2 characters")
    @NotBlank(message = "Surname is mandatory")
    private String surname;

    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @NotBlank(message = "Date of birth is mandatory")
    @Column(name = "date_of_birth", nullable = false)
    private String dateOfBirth;

    @NotBlank(message = "Country of origin is mandatory")
    @Column(name = "country_of_origin", nullable = false)
    private String countryOrigin;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    @Column(unique = true, name = "email", nullable = false)
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "avatar", nullable = false)
    private String avatar;

    @NotBlank(message = "Phone number is mandatory")
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @ManyToMany
    @JsonIgnore
    @ToString.Exclude
    private List<CurrencyData> currencyData;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private List<Message> senderMessages;

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private List<Message> receiverMessages;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<AccessToken> accessTokens;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<BankIdentity> bankIdentities;

    public void encodePassword(PasswordEncoder encoder) {
        this.password = encoder.encode(password);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}