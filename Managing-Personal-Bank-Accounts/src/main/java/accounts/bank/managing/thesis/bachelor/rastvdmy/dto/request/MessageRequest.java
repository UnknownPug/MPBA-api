package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;

public record MessageRequest(
        @JsonProperty("sender_id")
        Long senderId,

        @JsonProperty("receiver_id")
        Long receiverId,

        String content) {
}
