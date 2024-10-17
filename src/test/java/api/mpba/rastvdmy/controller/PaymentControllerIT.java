package api.mpba.rastvdmy.controller;

import api.mpba.rastvdmy.Application;
import api.mpba.rastvdmy.config.SecurityConfig;
import api.mpba.rastvdmy.controller.mapper.PaymentMapper;
import api.mpba.rastvdmy.dto.request.PaymentParamsRequest;
import api.mpba.rastvdmy.dto.request.PaymentRequest;
import api.mpba.rastvdmy.dto.response.PaymentResponse;
import api.mpba.rastvdmy.entity.Payment;
import api.mpba.rastvdmy.entity.enums.*;
import api.mpba.rastvdmy.service.JwtService;
import api.mpba.rastvdmy.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = {Application.class, SecurityConfig.class}
)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class PaymentControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @InjectMocks
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private PaymentMapper paymentMapper;

    private String jwtToken;

    @BeforeEach
    public void setUp() {
        UserDetails userDetails = User.withUsername("john.doe@mpba.com")
                .password("Qwertyuiop123")
                .authorities("ROLE_DEFAULT")
                .build();

        when(jwtService.generateToken(userDetails)).thenReturn("mockedJwtToken");
        jwtToken = jwtService.generateToken(userDetails);
        when(jwtService.isTokenValid(jwtToken, userDetails)).thenReturn(true);

        // Set the security context
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    @Test
    public void callGetAllPayments_ShouldReturn_ListOfPayments() throws Exception {
        // Given
        UUID accountId = UUID.randomUUID();
        String bankName = "Bank1";

        Payment payment1 = Payment.builder()
                .id(UUID.randomUUID())
                .senderName("John Doe")
                .recipientName("Jane Doe")
                .dateTime(LocalDate.now())
                .description("Payment 1")
                .amount(BigDecimal.valueOf(100))
                .type(PaymentType.BANK_TRANSFER)
                .status(FinancialStatus.RECEIVED)
                .currency(Currency.USD)
                .build();

        Payment payment2 = Payment.builder()
                .id(UUID.randomUUID())
                .senderName("Jane Doe")
                .recipientName("John Doe")
                .dateTime(LocalDate.now())
                .description("Payment 2")
                .amount(BigDecimal.valueOf(200))
                .type(PaymentType.CARD_PAYMENT)
                .status(FinancialStatus.RECEIVED)
                .currency(Currency.USD)
                .build();

        List<Payment> payments = List.of(payment1, payment2);

        // When
        when(paymentService.getAllPayments(any(HttpServletRequest.class),
                eq(bankName), eq(accountId))).thenReturn(payments);

        when(paymentMapper.toResponse(any(PaymentRequest.class))).thenAnswer(invocation -> {
            PaymentRequest request = invocation.getArgument(0);
            return new PaymentResponse(
                    request.id(),
                    request.senderName(),
                    request.recipientName(),
                    request.dateTime(),
                    request.description(),
                    request.amount(),
                    request.type(),
                    request.status(),
                    request.currency()
            );
        });

        // Then
        mockMvc.perform(
                        get("/api/v1/{accountId}/payments/{bankName}", accountId, bankName)
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$[0].amount").value(payment1.getAmount()))
                .andExpect(jsonPath("$[0].date_time").value(payment1.getDateTime().toString()))
                .andExpect(jsonPath("$[0].currency").value(payment1.getCurrency().toString()))
                .andExpect(jsonPath("$[0].type").value(payment1.getType().toString()))
                .andExpect(jsonPath("$[0].status").value(payment1.getStatus().toString()))
                .andExpect(jsonPath("$[1].sender_name").value(payment2.getSenderName()))
                .andExpect(jsonPath("$[1].recipient_name").value(payment2.getRecipientName()))
                .andExpect(jsonPath("$[1].description").value(payment2.getDescription()))
                .andExpect(jsonPath("$[1].amount").value(payment2.getAmount()))
                .andExpect(jsonPath("$[1].date_time").value(payment2.getDateTime().toString()))
                .andExpect(jsonPath("$[1].currency").value(payment2.getCurrency().toString()))
                .andExpect(jsonPath("$[1].type").value(payment2.getType().toString()))
                .andExpect(jsonPath("$[1].status").value(payment2.getStatus().toString()));
    }

    @Test
    public void createPayment_ShouldReturn_CreatedPayment_ForCardPayment() throws Exception {
        // Given
        UUID accountId = UUID.randomUUID();
        PaymentParamsRequest paymentParamsRequest = new PaymentParamsRequest(
                null, null, null, UUID.randomUUID(), PaymentType.CARD_PAYMENT
        );
        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .senderName("Jane Doe")
                .recipientName("John Doe")
                .dateTime(LocalDate.now())
                .description("Payment 2")
                .amount(BigDecimal.valueOf(200))
                .type(PaymentType.CARD_PAYMENT)
                .status(FinancialStatus.RECEIVED)
                .currency(Currency.USD)
                .build();

        // When
        when(paymentService.createCardPayment(any(HttpServletRequest.class),
                eq(accountId), eq(paymentParamsRequest.cardId()))).thenReturn(payment);

        when(paymentMapper.toResponse(any(PaymentRequest.class))).thenReturn(new PaymentResponse(
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

        // Then
        mockMvc.perform(
                        post("/api/v1/{accountId}/payments", accountId)
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(paymentParamsRequest))
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sender_name").value(payment.getSenderName()))
                .andExpect(jsonPath("$.recipient_name").value(payment.getRecipientName()))
                .andExpect(jsonPath("$.description").value(payment.getDescription()))
                .andExpect(jsonPath("$.amount").value(payment.getAmount()));
    }

    @Test
    public void createPayment_ShouldReturn_CreatedPayment_ForBankTransfer() throws Exception {
        // Given
        UUID accountId = UUID.randomUUID();
        PaymentParamsRequest paymentParamsRequest = new PaymentParamsRequest(
                "123456789",
                "Bank Transfer",
                new BigDecimal("200.00"),
                null,
                PaymentType.BANK_TRANSFER
        );
        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .senderName("Jane Doe")
                .recipientName("John Doe")
                .dateTime(LocalDate.now())
                .description("Payment 2")
                .amount(BigDecimal.valueOf(200))
                .type(PaymentType.BANK_TRANSFER)
                .status(FinancialStatus.RECEIVED)
                .currency(Currency.USD)
                .build();

        // When
        when(paymentService.createBankTransfer(any(HttpServletRequest.class),
                eq(accountId), eq(paymentParamsRequest.recipientNumber()), eq(paymentParamsRequest.amount()),
                eq(paymentParamsRequest.description()))).thenReturn(payment);
        when(paymentMapper.toResponse(any(PaymentRequest.class))).thenReturn(new PaymentResponse(
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

        // Then
        mockMvc.perform(
                        post("/api/v1/{accountId}/payments", accountId)
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(paymentParamsRequest))
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sender_name").value(payment.getSenderName()))
                .andExpect(jsonPath("$.recipient_name").value(payment.getRecipientName()))
                .andExpect(jsonPath("$.description").value(payment.getDescription()))
                .andExpect(jsonPath("$.amount").value(payment.getAmount()));
    }
}
