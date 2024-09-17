package api.mpba.rastvdmy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * This class represents a message in the banking system.
 * @param receiverEmail The name of the receiver.
 * @param content The content of the message.
 */
public record MessageRequest(
        @JsonProperty("receiver_email")
        String receiverEmail,

        String content,

        @JsonProperty("sender_email")
        String senderEmail,

        LocalDateTime timestamp) {}
