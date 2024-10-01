package api.mpba.rastvdmy.dto.response;

import api.mpba.rastvdmy.entity.enums.CardCategory;
import api.mpba.rastvdmy.entity.enums.CardStatus;
import api.mpba.rastvdmy.entity.enums.CardType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDate;
import java.util.UUID;

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
