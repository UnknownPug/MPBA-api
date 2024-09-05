package api.mpba.rastvdmy.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;


/**
 * This class represents a message in the banking system.
 * @param receiverName The name of the receiver.
 * @param content The content of the message.
 */
public record MessageRequest(
        @NotBlank(message = "Sender name is mandatory")
        @JsonProperty(namespace = "receiver_name")
        String receiverName,

        @NotBlank(message = "Content is mandatory")
        String content
) {}
