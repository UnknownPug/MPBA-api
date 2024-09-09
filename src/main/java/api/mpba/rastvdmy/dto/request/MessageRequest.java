package api.mpba.rastvdmy.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;


/**
 * This class represents a message in the banking system.
 * @param receiverEmail The name of the receiver.
 * @param content The content of the message.
 */
public record MessageRequest(

        @NotBlank(message = "Receiver email is mandatory")
        @JsonProperty("receiver_email")
        String receiverEmail,

        @NotBlank(message = "Content is mandatory")
        String content,

        @Nullable
        @JsonProperty("sender_email")
        String senderEmail
) {}
