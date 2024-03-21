package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request;

import java.math.BigDecimal;

public record BankLoanRequest(BigDecimal loanAmount, String currencyType) {
}
