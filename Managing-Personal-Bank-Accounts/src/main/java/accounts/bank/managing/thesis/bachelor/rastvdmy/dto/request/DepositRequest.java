package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Currency;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record DepositRequest(
        @JsonProperty("card_number")
        String cardNumber,

        String description,

        @JsonProperty("deposit_amount")
        BigDecimal depositAmount,

        Currency currency) {
}
