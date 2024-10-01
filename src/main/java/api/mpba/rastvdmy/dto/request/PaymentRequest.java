package api.mpba.rastvdmy.dto.request;

import api.mpba.rastvdmy.entity.enums.Currency;
import api.mpba.rastvdmy.entity.enums.FinancialStatus;
import api.mpba.rastvdmy.entity.enums.PaymentType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PaymentRequest(
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
