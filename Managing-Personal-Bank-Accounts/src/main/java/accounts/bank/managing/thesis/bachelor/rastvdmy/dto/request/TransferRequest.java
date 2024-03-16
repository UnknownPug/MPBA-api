package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request;

import java.math.BigDecimal;

public record TransferRequest(
        BigDecimal commission,
        String description,
        BigDecimal amount
) {
}
