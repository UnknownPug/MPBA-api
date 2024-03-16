package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CardRequest(
        BigDecimal balance,
        Integer cardNumber,
        LocalDateTime expirationDate,
        Integer cvv
) {
}
