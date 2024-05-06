package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a request for a message.
 * It contains the sender id, receiver id, and content of the message.
 */
public record MessageRequest(
        @JsonProperty("sender_id")
        Long senderId,

        @JsonProperty("receiver_id")
        Long receiverId,

        String content) {
}
