package api.mpba.rastvdmy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * This class represents a message in the banking system.
 * It contains information about the sender, receiver, content, and timestamp.
 *
 * @param receiverEmail The email of the message receiver.
 * @param content       The content of the message.
 * @param senderEmail   The email of the message sender.
 * @param timestamp     The time when the message was sent.
 */
public record MessageRequest(
        @JsonProperty("receiver_email")
        String receiverEmail,

        String content,

        @JsonProperty("sender_email")
        String senderEmail,

        LocalDateTime timestamp
) {}