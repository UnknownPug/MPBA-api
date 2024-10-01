package api.mpba.rastvdmy.dto.request;

import api.mpba.rastvdmy.entity.enums.PaymentType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentParamsRequest(

        @Nullable
        @JsonProperty("recipient_number")
        String recipientNumber,

        @Nullable
        String description,

        @Nullable
        BigDecimal amount,

        @Nullable
        @JsonProperty("card_id")
        UUID cardId,

        PaymentType type
) {}