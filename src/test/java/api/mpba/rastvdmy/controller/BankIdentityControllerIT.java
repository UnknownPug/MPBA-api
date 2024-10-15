package api.mpba.rastvdmy.controller;

import api.mpba.rastvdmy.Application;
import api.mpba.rastvdmy.config.SecurityConfig;
import api.mpba.rastvdmy.controller.mapper.BankIdentityMapper;
import api.mpba.rastvdmy.dto.request.BankIdentityRequest;
import api.mpba.rastvdmy.dto.response.BankIdentityResponse;
import api.mpba.rastvdmy.entity.BankIdentity;
import api.mpba.rastvdmy.service.BankIdentityService;
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

import java.util.List;
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
public class BankIdentityControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @InjectMocks
    private ObjectMapper objectMapper;

    @MockBean
    private BankIdentityService identityService;

    @MockBean
    private BankIdentityMapper identityMapper;

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
    public void callAddBank_ShouldReturn_CreatedBankIdentity() throws Exception {

        // Given
        BankIdentityRequest bankIdentityRequest = new BankIdentityRequest(
                "CzechBank",
                "123456",
                "CZBACZPP"
        );

        BankIdentity bankIdentity = BankIdentity.builder()
                .id(UUID.randomUUID())
                .bankName(bankIdentityRequest.bankName())
                .bankNumber(bankIdentityRequest.bankNumber())
                .swift(bankIdentityRequest.swift())
                .build();

        // When
        when(identityService.addBank(any(HttpServletRequest.class), eq(bankIdentityRequest))).thenReturn(bankIdentity);
        when(identityMapper.toResponse(any(BankIdentityRequest.class))).thenReturn(new BankIdentityResponse(
                bankIdentity.getBankName(),
                bankIdentity.getBankNumber(),
                bankIdentity.getSwift()
        ));

        // Then
        mockMvc.perform(
                        post("/api/v1/banks")
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bankIdentityRequest))
                )
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bank_name").value(bankIdentity.getBankName()))
                .andExpect(jsonPath("$.bank_number").value(bankIdentity.getBankNumber()))
                .andExpect(jsonPath("$.swift").value(bankIdentity.getSwift()));
    }

    @Test
    public void callGetBanks_ShouldReturn_ListOfBankIdentities() throws Exception {

        // Given
        BankIdentity bankIdentity1 = BankIdentity.builder()
                .id(UUID.randomUUID())
                .bankName("Bank1")
                .bankNumber("111111")
                .swift("SWIFT1")
                .build();

        BankIdentity bankIdentity2 = BankIdentity.builder()
                .id(UUID.randomUUID())
                .bankName("Bank2")
                .bankNumber("222222")
                .swift("SWIFT2")
                .build();

        List<BankIdentity> bankIdentities = List.of(bankIdentity1, bankIdentity2);

        // When
        when(identityService.getBanks(any(HttpServletRequest.class))).thenReturn(bankIdentities);
        when(identityMapper.toResponse(any(BankIdentityRequest.class))).thenAnswer(invocation -> {
            BankIdentityRequest request = invocation.getArgument(0);
            return new BankIdentityResponse(request.bankName(), request.bankNumber(), request.swift());
        });

        // Then
        mockMvc.perform(
                        get("/api/v1/banks")
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].bank_name").value(bankIdentity1.getBankName()))
                .andExpect(jsonPath("$[0].bank_number").value(bankIdentity1.getBankNumber()))
                .andExpect(jsonPath("$[0].swift").value(bankIdentity1.getSwift()))
                .andExpect(jsonPath("$[1].bank_name").value(bankIdentity2.getBankName()))
                .andExpect(jsonPath("$[1].bank_number").value(bankIdentity2.getBankNumber()))
                .andExpect(jsonPath("$[1].swift").value(bankIdentity2.getSwift()));
    }

    @Test
    public void callGetBankByName_ShouldReturn_BankIdentity() throws Exception {

        // Given
        String bankName = "Bank1";
        BankIdentity bankIdentity = BankIdentity.builder()
                .id(UUID.randomUUID())
                .bankName(bankName)
                .bankNumber("111111")
                .swift("SWIFT1")
                .build();

        // When
        when(identityService.getBankByName(any(HttpServletRequest.class), eq(bankName))).thenReturn(bankIdentity);
        when(identityMapper.toResponse(any(BankIdentityRequest.class))).thenReturn(new BankIdentityResponse(
                bankIdentity.getBankName(),
                bankIdentity.getBankNumber(),
                bankIdentity.getSwift()
        ));

        // Then
        mockMvc.perform(
                        get("/api/v1/banks/{name}", bankName)
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bank_name").value(bankIdentity.getBankName()))
                .andExpect(jsonPath("$.bank_number").value(bankIdentity.getBankNumber()))
                .andExpect(jsonPath("$.swift").value(bankIdentity.getSwift()));
    }

    @Test
    public void callDeleteBank_ShouldReturn_NoContent() throws Exception {

        // Given
        String bankName = "Bank1";

        // When
        doNothing().when(identityService).deleteBank(any(HttpServletRequest.class), eq(bankName));

        // Then
        mockMvc.perform(
                        delete("/api/v1/banks/{name}", bankName)
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());
    }
}