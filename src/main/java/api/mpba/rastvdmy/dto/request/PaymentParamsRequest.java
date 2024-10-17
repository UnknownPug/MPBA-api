package api.mpba.rastvdmy.dto.request;

import api.mpba.rastvdmy.entity.enums.PaymentType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * This class represents the parameters required for processing a payment in the banking system.
 *
 * @param recipientNumber The recipient's account or card number.
 * @param description     The description or note for the payment.
 * @param amount          The amount to be transferred.
 * @param cardId          The unique identifier of the card being used for the payment.
 * @param type            The type of payment being processed.
 */
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