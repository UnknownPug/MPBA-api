package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * This class represents a request for a transfer.
 * It contains the id, sender id, reference number, receiver card number, description, and amount of the transfer.
 */
public record TransferRequest(
        Long id,

        @JsonProperty("sender_id")
        Long senderId,

        @JsonProperty("reference_number")
        String referenceNumber,

        @JsonProperty("receiver_card_number")
        String receiverCardNumber,

        String description,

        BigDecimal amount) {
}
