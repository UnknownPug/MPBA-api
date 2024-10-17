package api.mpba.rastvdmy.dto.request;

import api.mpba.rastvdmy.entity.enums.Currency;
import api.mpba.rastvdmy.entity.enums.FinancialStatus;
import api.mpba.rastvdmy.entity.enums.PaymentType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * This class represents a payment request in the banking system.
 *
 * @param id            The unique identifier for the payment.
 * @param senderName    The name of the payment sender.
 * @param recipientName The name of the payment recipient.
 * @param dateTime      The date and time of the payment.
 * @param description   A description or note associated with the payment.
 * @param amount        The amount of the payment.
 * @param type          The type of the payment (e.g., transfer, purchase).
 * @param status        The financial status of the payment (e.g., pending, completed).
 * @param currency      The currency in which the payment is made.
 */
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
