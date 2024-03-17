package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "currency_data")
public class CurrencyData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_bank_currency", nullable = false)
    @ToString.Exclude
    private User bankCurrency;
}
