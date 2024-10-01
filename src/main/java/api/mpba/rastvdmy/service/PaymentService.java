package api.mpba.rastvdmy.service;

import api.mpba.rastvdmy.entity.Payment;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentService {

    List<Payment> getAllPayments(HttpServletRequest request, String bankName, UUID accountId);

    Payment getPaymentById(HttpServletRequest request, String bankName, UUID accountId, UUID paymentId);

    Payment createBankTransfer(HttpServletRequest request, UUID accountId, String recipientNumber,
                               BigDecimal amount, String description) throws Exception;

    Payment createCardPayment(HttpServletRequest request, UUID accountId, UUID cardId) throws Exception;
}
