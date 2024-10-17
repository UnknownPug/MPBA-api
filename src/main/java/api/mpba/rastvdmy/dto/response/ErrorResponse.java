package api.mpba.rastvdmy.dto.response;

/**
 * This class represents an error response in the application.
 *
 * @param message A descriptive message about the error.
 * @param value   The value associated with the error, providing additional context.
 */
public record ErrorResponse(
        String message,

        String value
) {}
