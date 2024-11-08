package api.mpba.rastvdmy.controller;

import api.mpba.rastvdmy.controller.mapper.PaymentMapper;
import api.mpba.rastvdmy.dto.request.PaymentParamsRequest;
import api.mpba.rastvdmy.dto.request.PaymentRequest;
import api.mpba.rastvdmy.dto.response.PaymentResponse;
import api.mpba.rastvdmy.entity.Payment;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for handling payment-related requests.
 * <p>
 * This controller provides endpoints for retrieving, creating, and managing payments
 * associated with a specific account. It ensures that the user has the appropriate
 * role to access payment operations.
 * </p>
 */
@Slf4j
@RestController
@PreAuthorize("hasRole('ROLE_DEFAULT')")
@RequestMapping(path = "/api/v1/{accountId}/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    /**
     * Constructor for the PaymentController.
     *
     * @param paymentService The service for payment operations.
     * @param paymentMapper  The mapper to convert between Payment and PaymentResponse.
     */
    public PaymentController(PaymentService paymentService, PaymentMapper paymentMapper) {
        this.paymentService = paymentService;
        this.paymentMapper = paymentMapper;
    }

    /**
     * Retrieves all payments associated with the specified account.
     *
     * @param request   The HTTP servlet request.
     * @param accountId The UUID of the account.
     * @param bankName  The name of the bank.
     * @return A response entity containing a list of payment responses.
     */
    @GetMapping(path = "/{bankName}", produces = "application/json")
    public ResponseEntity<List<PaymentResponse>> getAllPayments(HttpServletRequest request,
                                                                @PathVariable("accountId") UUID accountId,
                                                                @PathVariable("bankName") String bankName) {

        logInfo("Getting all payments from account and cards...");

        List<Payment> payments = paymentService.getAllPayments(request, bankName, accountId);

        List<PaymentResponse> paymentResponses = payments.stream()
                .map(payment -> paymentMapper.toResponse(convertToPaymentRequest(payment)))
                .toList();

        return ResponseEntity.ok(paymentResponses);
    }

    /**
     * Retrieves a specific payment by its ID.
     *
     * @param request   The HTTP servlet request.
     * @param bankName  The name of the bank.
     * @param accountId The UUID of the account.
     * @param paymentId The UUID of the payment.
     * @return A response entity containing the payment response.
     */
    @GetMapping(path = "/{bankName}/{id}", produces = "application/json")
    public ResponseEntity<PaymentResponse> getPaymentById(HttpServletRequest request,
                                                          @PathVariable("bankName") String bankName,
                                                          @PathVariable("accountId") UUID accountId,
                                                          @PathVariable("id") UUID paymentId) {
        logInfo("Getting payment info ...");
        Payment payment = paymentService.getPaymentById(request, bankName, accountId, paymentId);
        PaymentResponse paymentResponse = paymentMapper.toResponse(convertToPaymentRequest(payment));
        return ResponseEntity.ok(paymentResponse);
    }

    /**
     * Creates a new payment based on the provided parameters.
     *
     * @param request              The HTTP servlet request.
     * @param accountId            The UUID of the account.
     * @param paymentParamsRequest The request containing payment parameters.
     * @return A response entity containing the created payment response.
     * @throws ApplicationException If the payment type is invalid or not specified.
     */
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<PaymentResponse> createPayment(HttpServletRequest request,
                                                         @PathVariable("accountId") UUID accountId,
                                                         @RequestBody PaymentParamsRequest paymentParamsRequest
    ) throws Exception {

        if (paymentParamsRequest.type() == null || paymentParamsRequest.type().toString().isBlank()) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST, "Payment type is not chosen. Please, choose payment type."
            );
        }
        Payment payment;
        PaymentResponse paymentResponse;
        logInfo("Choosing payment type ...");

        switch (paymentParamsRequest.type()) {
            case BANK_TRANSFER -> {
                logInfo("Creating bank transfer ...");
                payment = paymentService.createBankTransfer(request, accountId,
                        paymentParamsRequest.recipientNumber(), paymentParamsRequest.amount(),
                        paymentParamsRequest.description());

                paymentResponse = paymentMapper.toResponse(convertToPaymentRequest(payment));
            }
            case CARD_PAYMENT -> {
                logInfo("Creating card payment ...");
                payment = paymentService.createCardPayment(request, accountId, paymentParamsRequest.cardId());
                paymentResponse = paymentMapper.toResponse(convertToPaymentRequest(payment));
            }
            default -> throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid payment type.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentResponse);
    }

    /**
     * Converts a Payment object to a PaymentRequest object.
     *
     * @param payment The payment object to convert.
     * @return A PaymentRequest object.
     */
    private static PaymentRequest convertToPaymentRequest(Payment payment) {
        return new PaymentRequest(
                payment.getId(),
                payment.getSenderName(),
                payment.getRecipientName(),
                payment.getDateTime(),
                payment.getDescription(),
                payment.getAmount(),
                payment.getType(),
                payment.getStatus(),
                payment.getCurrency());
    }

    /**
     * Logs informational messages to the console.
     *
     * @param message The message to log.
     * @param args    Optional arguments to format the message.
     */
    private void logInfo(String message, Object... args) {
        log.info(message, args);
    }
}
