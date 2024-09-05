package api.mpba.rastvdmy.dto.response;

import java.time.LocalDate;

/**
 * This class represents a card in the banking system.
 * @param cardNumber The card number.
 * @param cvv The cvv of the card.
 * @param pin The pin of the card.
 */
public record CardResponse(
        String cardNumber,

        String cvv,

        String pin,

        LocalDate startDate,

        LocalDate expirationDate
) {}
