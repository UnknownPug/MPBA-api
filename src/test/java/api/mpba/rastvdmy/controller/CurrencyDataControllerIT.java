package api.mpba.rastvdmy.controller;

import api.mpba.rastvdmy.Application;
import api.mpba.rastvdmy.config.SecurityConfig;
import api.mpba.rastvdmy.controller.mapper.CurrencyDataMapper;
import api.mpba.rastvdmy.dto.request.CurrencyDataRequest;
import api.mpba.rastvdmy.dto.response.CurrencyDataResponse;
import api.mpba.rastvdmy.entity.CurrencyData;
import api.mpba.rastvdmy.service.CurrencyDataService;
import api.mpba.rastvdmy.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
public class CurrencyDataControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CurrencyDataService currencyDataService;

    @MockBean
    private CurrencyDataMapper currencyDataMapper;

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
    public void updateAndFetchAllCurrencies_ShouldReturn_ListOfCurrencyData() throws Exception {
        // Given
        CurrencyData currencyData1 = CurrencyData.builder()
                .id(UUID.randomUUID())
                .currency("USD")
                .rate(BigDecimal.valueOf(1))
                .build();

        CurrencyData currencyData2 = CurrencyData.builder()
                .id(UUID.randomUUID())
                .currency("EUR")
                .rate(BigDecimal.valueOf(0.85))
                .build();

        List<CurrencyData> currencies = List.of(currencyData1, currencyData2);

        // When
        when(currencyDataService.findAllCurrencies(any(HttpServletRequest.class))).thenReturn(currencies);
        when(currencyDataMapper.toResponse(any(CurrencyDataRequest.class))).thenAnswer(invocation -> {
            CurrencyDataRequest request = invocation.getArgument(0);
            return new CurrencyDataResponse(request.currency(), request.rate());
        });

        // Then
        mockMvc.perform(
                        get("/api/v1/currency-data")
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].currency").value(currencyData1.getCurrency()))
                .andExpect(jsonPath("$[0].rate").value(currencyData1.getRate()))
                .andExpect(jsonPath("$[1].currency").value(currencyData2.getCurrency()))
                .andExpect(jsonPath("$[1].rate").value(currencyData2.getRate()));
    }

    @Test
    public void findByCurrency_ShouldReturn_CurrencyData() throws Exception {
        // Given
        String currencyType = "USD";
        CurrencyData currencyData = CurrencyData.builder()
                .id(UUID.randomUUID())
                .currency("USD")
                .rate(new BigDecimal("1.0"))
                .build();

        // When
        when(currencyDataService.findByCurrency(
                any(HttpServletRequest.class), eq(currencyType))).thenReturn(currencyData);
        when(currencyDataMapper.toResponse(any(CurrencyDataRequest.class))).thenReturn(new CurrencyDataResponse(
                currencyData.getCurrency(), currencyData.getRate()
        ));

        // Then
        mockMvc.perform(
                        get("/api/v1/currency-data/{currency}", currencyType)
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currency").value(currencyData.getCurrency()))
                .andExpect(jsonPath("$.rate").value(currencyData.getRate()));
    }
}
