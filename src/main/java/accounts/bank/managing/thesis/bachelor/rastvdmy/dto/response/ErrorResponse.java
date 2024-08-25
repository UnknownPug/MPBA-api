package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.response;

/**
 * Error response
 * @param message The message of the error
 * @param value The value of the error
 */
public record ErrorResponse(
        String message,

        String value
) {}
