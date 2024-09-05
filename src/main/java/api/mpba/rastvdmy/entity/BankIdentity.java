package api.mpba.rastvdmy.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * This class represents a bank identity in the banking system.
 * It contains the id, bank name, bank number, user, access tokens, and bank accounts.
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
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * The name of the bank.
     */
    @NotBlank
    @Column(name = "bank_name", nullable = false)
    private String bankName;

    /**
     * The number of the bank.
     */
    @NotBlank
    @Column(name = "bank_number", nullable = false)
    private String bankNumber;

    /**
     * The user of the bank identity.
     */
    @ManyToOne
    @JsonIgnore
    @ToString.Exclude
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The bank accounts of the bank identity.
     */
    @OneToMany(mappedBy = "bankIdentity", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<BankAccount> bankAccounts;
}