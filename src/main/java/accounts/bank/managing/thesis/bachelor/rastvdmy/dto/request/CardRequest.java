package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * This class represents a request for a card.
 * It contains the currency type, card type, card number, pin, and balance of the card.
 */
public record CardRequest(
        String currency,

        @JsonProperty("card_type")
        String type,

        @JsonProperty("card_number")
        String cardNumber,

        Integer pin,

        BigDecimal balance) {
}
