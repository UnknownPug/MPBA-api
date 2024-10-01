package api.mpba.rastvdmy.dto.response;


import api.mpba.rastvdmy.entity.enums.FinancialStatus;
import api.mpba.rastvdmy.entity.enums.PaymentType;
import api.mpba.rastvdmy.entity.enums.Currency;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@JsonPropertyOrder({
        "id",
        "sender_name",
        "recipient_name",
        "currency",
        "amount",
        "description",
        "date_time",
        "type",
        "status",
})
public record PaymentResponse(
        UUID id,

        @JsonProperty("sender_name")
        String senderName,

        @JsonProperty("recipient_name")
        String recipientName,

        @JsonProperty("date_time")
        LocalDate dateTime,

        String description,

        BigDecimal amount,

        PaymentType type,

        FinancialStatus status,

        Currency currency
) {}
