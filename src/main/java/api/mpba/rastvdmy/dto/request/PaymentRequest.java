package api.mpba.rastvdmy.dto.request;

import api.mpba.rastvdmy.entity.enums.PaymentType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentRequest(
        BigDecimal amount,

        @JsonProperty("date_time")
        LocalDate dateTime,

        PaymentType type,

        @JsonProperty("sender_name")
        String senderName,

        @JsonProperty("recipient_name")
        String recipientName,

        String description,

        @JsonProperty("sender_number")
        String senderNumber,

        @JsonProperty("recipient_number")
        String recipientNumber,

        @JsonProperty("sender_pin")
        String senderPin,

        @JsonProperty("sender_cvv")
        String senderCvv
) {}
