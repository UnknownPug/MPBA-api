package api.mpba.rastvdmy.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * This class represents a bank identity in the banking system.
 * It contains the id, bank name, bank number, SWIFT code, associated user,
 * access tokens, and the bank accounts linked to the identity.
 */
@Setter
@Getter
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bank_identity")
public class BankIdentity implements Serializable {

    /**
     * The id of the bank identity.
     * This serves as the primary key and uniquely identifies the bank identity.
     */
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * The name of the bank associated with this bank identity.
     * This field is mandatory and should be provided as a valid bank name.
     */
    @Column(name = "bank_name", nullable = false)
    private String bankName;

    /**
     * The number of the bank, which may be used for identification purposes.
     * This field is mandatory and should be provided as a valid bank number.
     */
    @Column(name = "bank_number", nullable = false)
    private String bankNumber;

    /**
     * The SWIFT code of the bank.
     * This field is mandatory and identifies the bank in international transactions.
     */
    @Column(name = "swift", nullable = false)
    private String swift;

    /**
     * The user profile associated with this bank identity.
     * This establishes a many-to-one relationship with the UserProfile entity,
     * indicating that a user can have multiple bank identities.
     */
    @ManyToOne
    @JsonIgnore
    @ToString.Exclude
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile userProfile;

    /**
     * The bank accounts linked to this bank identity.
     * This establishes a one-to-many relationship with the BankAccount entity,
     * allowing for the management of multiple bank accounts under a single bank identity.
     */
    @OneToMany(mappedBy = "bankIdentity", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    private List<BankAccount> bankAccounts;
}
