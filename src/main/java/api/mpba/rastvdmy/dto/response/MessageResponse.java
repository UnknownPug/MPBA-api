package api.mpba.rastvdmy.dto.response;

/**
 * This class represents a response for a message.
 * @param receiverName The name of the receiver.
 * @param content The content of the message.
 */
public record MessageResponse(
        String receiverName,

        String content
) {}
