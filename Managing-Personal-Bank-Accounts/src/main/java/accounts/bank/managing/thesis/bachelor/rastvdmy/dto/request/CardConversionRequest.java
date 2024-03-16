package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CardConversionRequest(
        BigDecimal prevAmount,
        BigDecimal targetAmount,
        BigDecimal commission) {
}
