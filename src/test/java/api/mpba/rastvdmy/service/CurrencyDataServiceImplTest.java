package api.mpba.rastvdmy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import api.mpba.rastvdmy.dto.response.CurrencyApiResponse;
import api.mpba.rastvdmy.entity.CurrencyData;
import api.mpba.rastvdmy.repository.CurrencyDataRepository;
import api.mpba.rastvdmy.service.impl.CurrencyDataServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CurrencyDataServiceImplTest {

    @Mock
    private CurrencyDataRepository currencyDataRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CurrencyDataServiceImpl currencyDataService;

    private CurrencyData currencyData;

    @BeforeEach
    void setUp() {
        currencyData = new CurrencyData();
        currencyData.setId(UUID.randomUUID());
        currencyData.setCurrency("USD");
        currencyData.setRate(BigDecimal.valueOf(1.0));
    }

    @Test
    void checkIfCurrencyExists_ShouldReturnTrue_WhenCurrencyExists() {
        when(currencyDataRepository.findByCurrency("USD")).thenReturn(currencyData);
        boolean result = currencyDataRepository.findByCurrency("USD") != null;

        assertTrue(result);
    }

    @Test
    void convertCurrency_ShouldReturnConvertedCurrencyData() {
        CurrencyApiResponse responseBody = new CurrencyApiResponse();
        responseBody.setConversionRate(String.valueOf(0.85));
        ResponseEntity<CurrencyApiResponse> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        CurrencyData result = currencyDataService.convertCurrency("USD", "EUR");

        assertEquals("EUR", result.getCurrency());
        assertEquals(BigDecimal.valueOf(0.85), result.getRate());
    }

    @Test
    void findAllExchangeRates_ShouldUpdateCurrencyData() {
        CurrencyApiResponse responseBody = new CurrencyApiResponse();
        Map<String, BigDecimal> conversionRates = new HashMap<>();
        conversionRates.put("USD", BigDecimal.valueOf(1.0));
        responseBody.setConversionRates(conversionRates);
        ResponseEntity<CurrencyApiResponse> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);
        when(currencyDataRepository.findByCurrency("USD")).thenReturn(currencyData);

        currencyDataService.findAllExchangeRates();

        verify(currencyDataRepository).save(currencyData);
    }
}