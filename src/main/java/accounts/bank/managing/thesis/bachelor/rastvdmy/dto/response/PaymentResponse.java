package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentResponse(
        BigDecimal amount,

        LocalDate dateTime,

        String paymentType,

        String senderName,

        String recipientName,

        String description,

        String senderNumber,

        String recipientNumber,

        String senderPin,

        String senderCvv
) {}
