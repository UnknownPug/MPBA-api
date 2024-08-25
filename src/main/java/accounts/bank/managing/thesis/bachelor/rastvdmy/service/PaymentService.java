package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.PaymentRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Payment;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    List<Payment> getPayments(HttpServletRequest request);

    Page<Payment> filterAndSortTransfers(HttpServletRequest request, PageRequest pageable);

    Payment getPaymentById(HttpServletRequest request, UUID paymentId);

    Payment createBankPayment(HttpServletRequest request, PaymentRequest paymentRequest, String recipientNumber);

    Payment createCardPayment(HttpServletRequest request, PaymentRequest paymentRequest);
}
