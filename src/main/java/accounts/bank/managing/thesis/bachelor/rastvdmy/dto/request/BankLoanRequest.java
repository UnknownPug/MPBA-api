package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * This class represents a request for a bank loan.
 * It contains the loan amount, currency type, reference number, start date, and expiration date of the loan.
 */
public record BankLoanRequest(
        @JsonProperty("loan_amount")
        BigDecimal loanAmount,

        @JsonProperty("currency_type")
        String currencyType,

        @JsonProperty("reference_number")
        String referenceNumber,

        @JsonProperty("start_date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate startDate,

        @JsonProperty("expiration_date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate expirationDate) {

}
