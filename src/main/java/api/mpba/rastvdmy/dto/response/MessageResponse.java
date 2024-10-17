package api.mpba.rastvdmy.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * This class represents a response for a message in the banking system.
 *
 * @param receiverEmail The email of the message receiver.
 * @param content       The content of the message.
 * @param senderEmail   The email of the message sender.
 * @param timestamp     The timestamp of when the message was sent.
 */
public record MessageResponse(
        @JsonProperty("receiver_email")
        String receiverEmail,

        String content,

        @JsonProperty("sender_email")
        String senderEmail,

        LocalDateTime timestamp
) {}
