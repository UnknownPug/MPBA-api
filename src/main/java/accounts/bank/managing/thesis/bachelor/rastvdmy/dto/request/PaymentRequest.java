package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.enums.PaymentType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentRequest(
        @NotBlank
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotBlank
        @JsonProperty("date_time")
        LocalDate dateTime,

        @NotBlank(message = "Payment type must be specified")
        PaymentType type,

        @Nullable
        @JsonProperty("sender_name")
        String senderName,

        @Nullable
        @JsonProperty("recipient_name")
        String recipientName,

        @Nullable
        String description,

        @Nullable
        @JsonProperty("sender_number")
        String senderNumber,

        @Nullable
        @JsonProperty("recipient_number")
        String recipientNumber,

        @Nullable
        @JsonProperty("sender_pin")
        String senderPin,

        @Nullable
        @JsonProperty("sender_cvv")
        String senderCvv
) {}
