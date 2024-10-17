package api.mpba.rastvdmy.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * This class represents the currency data.
 * It contains the id, currency, rate, and a list of users associated with the currency.
 */
@Getter
@Setter
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "currency_data")
public class CurrencyData implements Serializable {

    /**
     * The id of the currency data.
     * This serves as the primary key and is generated using a UUID strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * The currency type represented by a string.
     * This field is mandatory and cannot be null.
     */
    @Column(name = "currency", nullable = false)
    private String currency;

    /**
     * The exchange rate of the currency.
     * This field is mandatory and should contain a valid numerical value.
     */
    @Column(name = "rate", nullable = false)
    private BigDecimal rate;

    /**
     * The list of users associated with the currency data.
     * This field represents a many-to-many relationship with the UserProfile entity.
     */
    @ManyToMany(mappedBy = "currencyData")
    @JsonIgnore
    @ToString.Exclude
    private List<UserProfile> userProfiles;
}
