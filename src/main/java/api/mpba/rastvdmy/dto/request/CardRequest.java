package api.mpba.rastvdmy.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * This class represents a card in the banking system.
 * @param cardNumber The card number.
 * @param cvv The cvv of the card.
 * @param pin The pin of the card.
 */
public record CardRequest(
        @NotBlank(message = "Card number is mandatory")
        @JsonProperty("card_number")
        String cardNumber,

        @NotBlank(message = "CVV is mandatory")
        String cvv,

        @NotBlank(message = "PIN is mandatory")
        String pin,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("expiration_date")
        LocalDate expirationDate
) {}
