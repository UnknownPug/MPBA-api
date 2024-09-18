package api.mpba.rastvdmy.dto.response;

import api.mpba.rastvdmy.entity.enums.CardCategory;
import api.mpba.rastvdmy.entity.enums.CardStatus;
import api.mpba.rastvdmy.entity.enums.CardType;
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
        LocalDate expirationDate,

        @JsonProperty("card_category")
        CardCategory cardCategory,

        @JsonProperty("card_type")
        CardType cardType,

        @JsonProperty("card_status")
        CardStatus cardStatus
) {}
