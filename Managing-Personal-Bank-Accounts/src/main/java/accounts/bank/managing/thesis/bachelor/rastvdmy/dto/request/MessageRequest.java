package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request;


public record MessageRequest(
        Long senderId,
        Long receiverId,
        String content) {
}
