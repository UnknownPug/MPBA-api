package api.mpba.rastvdmy.service;

import api.mpba.rastvdmy.entity.Payment;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing payment operations within the application.
 */
public interface PaymentService {

    /**
     * Retrieves all payments associated with a specific bank account.
     *
     * @param request   the HTTP request containing context information
     * @param bankName  the name of the bank associated with the payments
     * @param accountId the unique identifier of the bank account
     * @return a list of payments associated with the specified bank account
     */
    List<Payment> getAllPayments(HttpServletRequest request, String bankName, UUID accountId);

    /**
     * Retrieves a specific payment by its unique identifier.
     *
     * @param request   the HTTP request containing context information
     * @param bankName  the name of the bank associated with the payment
     * @param accountId the unique identifier of the bank account
     * @param paymentId the unique identifier of the payment
     * @return the payment associated with the specified payment ID
     */
    Payment getPaymentById(HttpServletRequest request, String bankName, UUID accountId, UUID paymentId);

    /**
     * Creates a bank transfer payment.
     *
     * @param request         the HTTP request containing context information
     * @param accountId       the unique identifier of the bank account initiating the transfer
     * @param recipientNumber the account number of the payment recipient
     * @param amount          the amount to be transferred
     * @param description     a description of the payment
     * @return the created Payment object
     * @throws Exception if there is an error while creating the bank transfer
     */
    Payment createBankTransfer(HttpServletRequest request, UUID accountId, String recipientNumber,
                               BigDecimal amount, String description) throws Exception;

    /**
     * Creates a card payment.
     *
     * @param request   the HTTP request containing context information
     * @param accountId the unique identifier of the bank account used for the card payment
     * @param cardId    the unique identifier of the card being used for the payment
     * @return the created Payment object
     * @throws Exception if there is an error while creating the card payment
     */
    Payment createCardPayment(HttpServletRequest request, UUID accountId, UUID cardId) throws Exception;
}
