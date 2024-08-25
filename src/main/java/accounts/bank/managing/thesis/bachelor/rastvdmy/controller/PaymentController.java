package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;

import accounts.bank.managing.thesis.bachelor.rastvdmy.controller.mapper.PaymentMapper;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.PaymentRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.response.PaymentResponse;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Payment;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@PreAuthorize("hasRole('ROLE_DEFAULT')")
@RequestMapping(path = "/api/v1/payments")
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
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<PaymentResponse>> getPayments(HttpServletRequest request) {
        logInfo("Getting all payments ...");
        List<Payment> payments = paymentService.getPayments(request);
        List<PaymentResponse> responses = payments.stream().map(payment -> paymentMapper.toResponse(new PaymentRequest(
                payment.getAmount(),
                payment.getDateTime(),
                payment.getType(),
                payment.getSenderName(),
                payment.getRecipientName(),
                payment.getDescription(),
                null,
                null,
                null,
                null

        ))).collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/filter", produces = "application/json")
    public ResponseEntity<Page<PaymentResponse>> filterPayments(
            HttpServletRequest request,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "asc") String sort) {

        logInfo("Filtering transfers ...");
        PageableState pageableState = parseSortOption(sort.trim().toLowerCase());
        PageRequest pageable = switch (pageableState) {
            case ASC -> {
                logInfo("Sorting transfers by amount in ascending order ...");
                yield PageRequest.of(page, size, Sort.by("dateTime").ascending());
            }
            case DESC -> {
                logInfo("Sorting transfers by amount in descending order ...");
                yield PageRequest.of(page, size, Sort.by("dateTime").descending());
            }
        };
        Page<Payment> payments = paymentService.filterAndSortTransfers(request, pageable);
        Page<PaymentResponse> paymentResponses = payments.map(payment -> paymentMapper.toResponse(new PaymentRequest(
                payment.getAmount(),
                payment.getDateTime(),
                payment.getType(),
                payment.getSenderName(),
                payment.getRecipientName(),
                payment.getDescription(),
                null,
                null,
                null,
                null
        )));
        return ResponseEntity.ok(paymentResponses);
    }

    private PageableState parseSortOption(String sortOption) {
        try {
            return PageableState.valueOf(sortOption.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid sort option. Use 'asc' or 'desc'.");
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<PaymentResponse> getPayment(HttpServletRequest request,
                                                      @PathVariable(value = "id") UUID paymentId) {
        logInfo("Getting payment info ...");
        Payment payment = paymentService.getPaymentById(request, paymentId);
        PaymentResponse paymentResponse = paymentMapper.toResponse(new PaymentRequest(
                payment.getAmount(),
                payment.getDateTime(),
                payment.getType(),
                payment.getSenderName(),
                payment.getRecipientName(),
                payment.getDescription(),
                null,
                null,
                null,
                null
        ));
        return ResponseEntity.ok(paymentResponse);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<PaymentResponse> createPayment(HttpServletRequest request,
                                                         @Valid @RequestBody PaymentRequest paymentRequest) {
        Payment payment;
        PaymentResponse paymentResponse;

        logInfo("Choosing payment type ...");
        switch (paymentRequest.type()) {
            case BANK_TRANSFER -> {
                logInfo("Creating bank transfer ...");
                payment = paymentService.createBankPayment(request, paymentRequest, paymentRequest.recipientNumber());
                paymentResponse = paymentMapper.toResponse(new PaymentRequest(
                        payment.getAmount(),
                        payment.getDateTime(),
                        payment.getType(),
                        payment.getSenderName(),
                        payment.getRecipientName(),
                        payment.getDescription(),
                        payment.getSenderAccount().getAccountNumber(),
                        payment.getRecipientAccount().getAccountNumber(),
                        null,
                        null
                ));
            }
            case CARD_PAYMENT ->  {
                logInfo("Creating card payment ...");
                payment = paymentService.createCardPayment(request, paymentRequest);
                paymentResponse = paymentMapper.toResponse(new PaymentRequest(
                        payment.getAmount(),
                        payment.getDateTime(),
                        payment.getType(),
                        payment.getSenderName(),
                        payment.getRecipientName(),
                        payment.getDescription(),
                        payment.getSenderCard().getCardNumber(),
                        null,
                        payment.getSenderCard().getPin(),
                        payment.getSenderCard().getCvv()
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
