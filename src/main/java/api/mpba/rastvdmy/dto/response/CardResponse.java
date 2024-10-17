package api.mpba.rastvdmy.dto.response;

import api.mpba.rastvdmy.entity.enums.CardCategory;
import api.mpba.rastvdmy.entity.enums.CardStatus;
import api.mpba.rastvdmy.entity.enums.CardType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDate;
import java.util.UUID;

/**
 * This class represents the response for a card in the banking system.
 *
 * @param id             The unique identifier of the card.
 * @param cardNumber     The card number.
 * @param cvv            The CVV of the card.
 * @param pin            The PIN associated with the card.
 * @param startDate      The date the card becomes active.
 * @param expirationDate The date the card expires.
 * @param cardCategory   The category of the card (e.g., credit, debit).
 * @param cardType       The type of the card (e.g., Visa, MasterCard).
 * @param cardStatus     The status of the card (e.g., active, blocked).
 */
@JsonPropertyOrder({
        "id",
        "card_number",
        "cvv",
        "pin",
        "start_date",
        "expiration_date",
        "card_category",
        "card_type",
        "card_status"
})
public record CardResponse(
        UUID id,

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
