package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request;

import java.math.BigDecimal;

public record TransferRequest(
        Long senderId,
        Long receiverId,
        String receiverCardNumber,
        String description,
        BigDecimal amount) {
}
