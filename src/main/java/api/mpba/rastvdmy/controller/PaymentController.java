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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@PreAuthorize("hasRole('ROLE_DEFAULT')")
@RequestMapping(path = "/api/v1/{accountId}/payments")
public class PaymentController {
    private final static Logger LOG = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @Autowired
    public PaymentController(PaymentService paymentService,
                             PaymentMapper paymentMapper) {
        this.paymentService = paymentService;
        this.paymentMapper = paymentMapper;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{bankName}", produces = "application/json")
    public ResponseEntity<List<PaymentResponse>> getAllPayments(HttpServletRequest request,
                                                                @PathVariable("accountId") UUID accountId,
                                                                @PathVariable("bankName") String bankName) {

        logInfo("Getting all payments from account and cards...");

        List<Payment> payments = paymentService.getAllPayments(request, bankName, accountId);

        List<PaymentResponse> paymentResponses = payments.stream().map(payment -> paymentMapper.toResponse(
                new PaymentRequest(
                        payment.getId(),
                        payment.getSenderName(),
                        payment.getRecipientName(),
                        payment.getDateTime(),
                        payment.getDescription(),
                        payment.getAmount(),
                        payment.getType(),
                        payment.getStatus(),
                        payment.getCurrency()))
                ).toList();

        return ResponseEntity.ok(paymentResponses);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{bankName}/{id}", produces = "application/json")
    public ResponseEntity<PaymentResponse> getPaymentById(HttpServletRequest request,
                                                          @PathVariable("bankName") String bankName,
                                                          @PathVariable("accountId") UUID accountId,
                                                          @PathVariable("id") UUID paymentId) {
        logInfo("Getting payment info ...");
        Payment payment = paymentService.getPaymentById(request, bankName, accountId, paymentId);
        PaymentResponse paymentResponse = paymentMapper.toResponse(new PaymentRequest(
                payment.getId(),
                payment.getSenderName(),
                payment.getRecipientName(),
                payment.getDateTime(),
                payment.getDescription(),
                payment.getAmount(),
                payment.getType(),
                payment.getStatus(),
                payment.getCurrency()
        ));
        return ResponseEntity.ok(paymentResponse);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<PaymentResponse> createPayment(HttpServletRequest request,
                                                         @PathVariable("accountId") UUID accountId,
                                                         @RequestBody
                                                         PaymentParamsRequest paymentParamsRequest) throws Exception {

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

                paymentResponse = paymentMapper.toResponse(new PaymentRequest(
                        payment.getId(),
                        payment.getSenderName(),
                        payment.getRecipientName(),
                        payment.getDateTime(),
                        payment.getDescription(),
                        payment.getAmount(),
                        payment.getType(),
                        payment.getStatus(),
                        payment.getCurrency()
                ));
            }
            case CARD_PAYMENT -> {
                logInfo("Creating card payment ...");
                payment = paymentService.createCardPayment(request, accountId, paymentParamsRequest.cardId());
                paymentResponse = paymentMapper.toResponse(new PaymentRequest(
                        payment.getId(),
                        payment.getSenderName(),
                        payment.getRecipientName(),
                        payment.getDateTime(),
                        payment.getDescription(),
                        payment.getAmount(),
                        payment.getType(),
                        payment.getStatus(),
                        payment.getCurrency()
                ));
            }
            default -> throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid payment type.");
        }
        return ResponseEntity.ok(paymentResponse);
    }

    private void logInfo(String message, Object... args) {
        LOG.info(message, args);
    }
}
