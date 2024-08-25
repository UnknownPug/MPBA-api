package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CurrencyDataResponse(
        UUID id,

        String currency,

        BigDecimal rate
) {}
