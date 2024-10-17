package api.mpba.rastvdmy.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * This class represents an access token for a user in the system.
 * It contains the token string, expiration date, and the associated user profile.
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
     * This serves as the primary key and uniquely identifies the access token.
     */
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * The token string.
     * This is the actual token used for authentication and authorization of the user.
     */
    @Column(name = "token", nullable = false)
    private String token;

    /**
     * The expiration date of the access token.
     * This indicates when the token becomes invalid and is no longer usable.
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    /**
     * The user associated with the access token.
     * This establishes a many-to-one relationship with the UserProfile entity,
     * indicating which user the access token belongs to.
     */
    @ManyToOne
    @JsonIgnore
    @ToString.Exclude
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile userProfile;
}
