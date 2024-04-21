package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * This class represents the currency data.
 * It contains the id, currency, rate, and a list of users.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "currency_data")
public class CurrencyData implements Serializable {

    /**
     * The id of the currency data.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * The currency of the currency data.
     */
    @Column(name = "currency", nullable = false)
    private String currency;

    /**
     * The rate of the currency.
     */
    @Column(name = "rate", nullable = false)
    private Double rate;

    /**
     * The list of users associated with the currency data.
     */
    @JsonIgnore
    @ToString.Exclude
    @ManyToMany(mappedBy = "currencyData")
    private List<User> users;
}
