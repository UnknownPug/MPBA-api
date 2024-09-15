package api.mpba.rastvdmy.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * This class represents a card in the banking system.
 * @param cardNumber The card number.
 * @param cvv The cvv of the card.
 * @param pin The pin of the card.
 */
public record CardResponse(
        @JsonProperty("card_number")
        String cardNumber,

        String cvv,

        String pin,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("expiration_date")
        LocalDate expirationDate
) {}
