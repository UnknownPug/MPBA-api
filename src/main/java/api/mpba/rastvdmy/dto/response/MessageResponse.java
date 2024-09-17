package api.mpba.rastvdmy.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record MessageResponse(
        @JsonProperty("receiver_email")
        String receiverEmail,

        String content,

        @JsonProperty("sender_email")
        String senderEmail,

        LocalDateTime timestamp
) {}
