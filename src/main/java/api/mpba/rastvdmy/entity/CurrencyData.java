package api.mpba.rastvdmy.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * This class represents the currency data.
 * It contains the id, currency, rate, and a list of users.
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "currency_data")
public class CurrencyData implements Serializable {

    /**
     * The id of the currency data.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "rate", nullable = false)
    private BigDecimal rate;

    /**
     * The list of users associated with the currency data.
     */
    @ManyToMany(mappedBy = "currencyData")
    @JsonIgnore
    @ToString.Exclude
    private List<User> users;
}
