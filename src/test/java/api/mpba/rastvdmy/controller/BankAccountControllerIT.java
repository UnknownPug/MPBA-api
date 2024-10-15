package api.mpba.rastvdmy.controller;

import api.mpba.rastvdmy.Application;
import api.mpba.rastvdmy.config.SecurityConfig;
import api.mpba.rastvdmy.controller.mapper.BankAccountMapper;
import api.mpba.rastvdmy.dto.request.BankAccountRequest;
import api.mpba.rastvdmy.dto.response.BankAccountResponse;
import api.mpba.rastvdmy.entity.BankAccount;
import api.mpba.rastvdmy.entity.enums.Currency;
import api.mpba.rastvdmy.service.BankAccountService;
import api.mpba.rastvdmy.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = {Application.class, SecurityConfig.class})
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class BankAccountControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @InjectMocks
    private ObjectMapper objectMapper;

    @MockBean
    private BankAccountService accountService;

    @MockBean
    private BankAccountMapper accountMapper;

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
    public void callGetUserAccounts_ShouldReturn_ListOfBankAccounts() throws Exception {
        // Given
        String bankName = "Bank1";

        BankAccount account1 = BankAccount.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.valueOf(1000))
                .accountNumber("123456")
                .iban("IBAN123")
                .currency(Currency.USD)
                .build();

        BankAccount account2 = BankAccount.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.valueOf(2000))
                .accountNumber("654321")
                .iban("IBAN321")
                .currency(Currency.EUR)
                .build();

        List<BankAccount> accounts = List.of(account1, account2);

        when(accountService.getUserAccounts(any(HttpServletRequest.class), eq(bankName))).thenReturn(accounts);
        when(accountMapper.toResponse(any(BankAccountRequest.class))).thenAnswer(invocation -> {
            BankAccountRequest request = invocation.getArgument(0);
            return new BankAccountResponse(
                    request.id(), request.accountNumber(), request.balance(), request.currency(), request.iban()
            );
        });

        // Then
        mockMvc.perform(
                        get("/api/v1/accounts/{name}", bankName)
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].account_number").value(account1.getAccountNumber()))
                .andExpect(jsonPath("$[0].balance").value(account1.getBalance()))
                .andExpect(jsonPath("$[0].currency").value(account1.getCurrency().toString()))
                .andExpect(jsonPath("$[0].iban").value(account1.getIban()))
                .andExpect(jsonPath("$[1].account_number").value(account2.getAccountNumber()))
                .andExpect(jsonPath("$[1].balance").value(account2.getBalance()))
                .andExpect(jsonPath("$[1].currency").value(account2.getCurrency().toString()))
                .andExpect(jsonPath("$[1].iban").value(account2.getIban()));
    }

    @Test
    public void callGetAccountById_ShouldReturn_AccountInfo() throws Exception {
        // Given
        String bankName = "Bank1";
        UUID accountId = UUID.randomUUID();
        String type = "visible";

        BankAccount account = BankAccount.builder()
                .id(accountId)
                .balance(BigDecimal.valueOf(1000))
                .accountNumber("123456")
                .iban("IBAN123")
                .currency(Currency.USD)
                .build();

        when(accountService.getAccountById(any(HttpServletRequest.class), eq(bankName), eq(accountId), eq(type))).thenReturn(account);
        when(accountMapper.toResponse(any(BankAccountRequest.class))).thenAnswer(invocation -> {
            BankAccountRequest request = invocation.getArgument(0);
            return new BankAccountResponse(
                    request.id(), request.accountNumber(), request.balance(), request.currency(), request.iban()
            );
        });

        // Then
        mockMvc.perform(
                        get("/api/v1/accounts/{name}/{accountId}", bankName, accountId)
                                .param("type", type)
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.account_number").value(account.getAccountNumber()))
                .andExpect(jsonPath("$.balance").value(account.getBalance()))
                .andExpect(jsonPath("$.currency").value(account.getCurrency().toString()))
                .andExpect(jsonPath("$.iban").value(account.getIban()));
    }

    @Test
    public void callGetAccountById_WithDefaultType_ShouldReturn_AccountInfo() throws Exception {
        // Given
        String bankName = "Bank1";
        UUID accountId = UUID.randomUUID();
        String defaultType = "non-visible";

        BankAccount account = BankAccount.builder()
                .id(accountId)
                .balance(BigDecimal.valueOf(1000))
                .accountNumber("123456")
                .iban("IBAN123")
                .currency(Currency.USD)
                .build();

        when(accountService.getAccountById(any(HttpServletRequest.class), eq(bankName), eq(accountId), eq(defaultType))).thenReturn(account);
        when(accountMapper.toResponse(any(BankAccountRequest.class))).thenAnswer(invocation -> {
            BankAccountRequest request = invocation.getArgument(0);
            return new BankAccountResponse(
                    request.id(), request.accountNumber(), request.balance(), request.currency(), request.iban()
            );
        });

        // Then
        mockMvc.perform(
                        get("/api/v1/accounts/{name}/{accountId}", bankName, accountId)
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.account_number").value(account.getAccountNumber()))
                .andExpect(jsonPath("$.balance").value(account.getBalance()))
                .andExpect(jsonPath("$.currency").value(account.getCurrency().toString()))
                .andExpect(jsonPath("$.iban").value(account.getIban()));
    }

    @Test
    public void callGetTotalBalance_ShouldReturn_TotalBalances() throws Exception {
        // Given
        Map<String, BigDecimal> totalBalances = Map.of("USD", new BigDecimal("3000.00"), "EUR", new BigDecimal("2000.00"));

        when(accountService.getTotalBalance(any(HttpServletRequest.class))).thenReturn(totalBalances);

        // Then
        mockMvc.perform(
                        get("/api/v1/accounts/total")
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.USD").value(totalBalances.get("USD").doubleValue()))
                .andExpect(jsonPath("$.EUR").value(totalBalances.get("EUR").doubleValue()));
    }

    @Test
    public void callAddAccount_ShouldReturn_CreatedBankAccount() throws Exception {
        // Given
        String bankName = "Bank1";
        BankAccountRequest accountRequest = new BankAccountRequest(UUID.randomUUID(), "123456", new BigDecimal("1000.00"), Currency.USD, "IBAN123");
        BankAccount account = BankAccount.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.valueOf(1000))
                .accountNumber("123456")
                .iban("IBAN123")
                .currency(Currency.USD)
                .build();

        when(accountService.addAccount(any(HttpServletRequest.class), eq(bankName))).thenReturn(account);
        when(accountMapper.toResponse(any(BankAccountRequest.class))).thenReturn(new BankAccountResponse(
                account.getId(), account.getAccountNumber(), account.getBalance(), account.getCurrency(), account.getIban()
        ));

        // Then
        mockMvc.perform(
                        post("/api/v1/accounts/{name}", bankName)
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(accountRequest))
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.account_number").value(account.getAccountNumber()))
                .andExpect(jsonPath("$.balance").value(account.getBalance()))
                .andExpect(jsonPath("$.currency").value(account.getCurrency().toString()))
                .andExpect(jsonPath("$.iban").value(account.getIban()));
    }

    @Test
    public void callRemoveAccount_ShouldReturn_NoContent() throws Exception {
        // Given
        String bankName = "Bank1";
        UUID accountId = UUID.randomUUID();

        // When
        doNothing().when(accountService).removeAccount(any(HttpServletRequest.class), eq(bankName), eq(accountId));

        // Then
        mockMvc.perform(
                        delete("/api/v1/accounts/{name}/{accountId}", bankName, accountId)
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());
    }

    @Test
    public void callRemoveAllAccounts_ShouldReturn_NoContent() throws Exception {
        // Given
        String bankName = "Bank1";

        // When
        doNothing().when(accountService).removeAllAccounts(any(HttpServletRequest.class), eq(bankName));

        // Then
        mockMvc.perform(
                        delete("/api/v1/accounts/{name}", bankName)
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());
    }
}