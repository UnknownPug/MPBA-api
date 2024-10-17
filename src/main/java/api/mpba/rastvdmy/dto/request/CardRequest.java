package api.mpba.rastvdmy.dto.request;

import api.mpba.rastvdmy.entity.enums.CardCategory;
import api.mpba.rastvdmy.entity.enums.CardStatus;
import api.mpba.rastvdmy.entity.enums.CardType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.UUID;

/**
 * This class represents a request for a card in the banking system.
 *
 * <p>It includes the following fields:</p>
 * <ul>
 *   <li><b>id</b>: The unique identifier for the card.</li>
 *   <li><b>cardNumber</b>: The card number, serialized to/from JSON with
 *       the key "card_number".</li>
 *   <li><b>cvv</b>: The CVV (Card Verification Value) of the card.</li>
 *   <li><b>pin</b>: The PIN (Personal Identification Number) of the card.</li>
 *   <li><b>startDate</b>: The start date of the card, serialized to/from JSON
 *       with the key "start_date".</li>
 *   <li><b>expirationDate</b>: The expiration date of the card, serialized
 *       to/from JSON with the key "expiration_date".</li>
 *   <li><b>cardCategory</b>: The category of the card (e.g., debit, credit),
 *       serialized to/from JSON with the key "card_category".</li>
 *   <li><b>cardType</b>: The type of the card (e.g., physical, virtual),
 *       serialized to/from JSON with the key "card_type".</li>
 *   <li><b>cardStatus</b>: The current status of the card (e.g., active,
 *       blocked), serialized to/from JSON with the key "card_status".</li>
 * </ul>
 *
 * @param id             The unique identifier for the card.
 * @param cardNumber     The card number.
 * @param cvv            The CVV of the card.
 * @param pin            The PIN of the card.
 * @param startDate      The start date of the card.
 * @param expirationDate The expiration date of the card.
 * @param cardCategory   The category of the card.
 * @param cardType       The type of the card.
 * @param cardStatus     The status of the card.
 */
public record CardRequest(
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
