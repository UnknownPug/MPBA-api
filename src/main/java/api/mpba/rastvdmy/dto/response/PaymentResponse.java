package api.mpba.rastvdmy.dto.response;


import api.mpba.rastvdmy.entity.enums.FinancialStatus;
import api.mpba.rastvdmy.entity.enums.PaymentType;
import api.mpba.rastvdmy.entity.enums.Currency;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * This class represents a response for a payment in the banking system.
 *
 * @param id            The unique identifier of the payment.
 * @param senderName    The name of the payment sender.
 * @param recipientName The name of the payment recipient.
 * @param dateTime      The date and time when the payment was made.
 * @param description   A description of the payment.
 * @param amount        The amount of money involved in the payment.
 * @param type          The type of the payment.
 * @param status        The financial status of the payment.
 * @param currency      The currency in which the payment is made.
 */
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
