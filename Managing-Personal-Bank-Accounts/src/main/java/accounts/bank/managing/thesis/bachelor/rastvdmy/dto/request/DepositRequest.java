package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Currency;

import java.math.BigDecimal;

public record DepositRequest(

        String cardNumber,
        String description,
        BigDecimal depositAmount,
        Currency currency) {
}
