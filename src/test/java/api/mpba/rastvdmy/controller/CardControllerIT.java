package api.mpba.rastvdmy.controller;

import api.mpba.rastvdmy.Application;
import api.mpba.rastvdmy.config.SecurityConfig;
import api.mpba.rastvdmy.controller.mapper.CardMapper;
import api.mpba.rastvdmy.dto.request.CardRequest;
import api.mpba.rastvdmy.dto.response.CardResponse;
import api.mpba.rastvdmy.entity.Card;
import api.mpba.rastvdmy.entity.enums.CardCategory;
import api.mpba.rastvdmy.entity.enums.CardStatus;
import api.mpba.rastvdmy.entity.enums.CardType;
import api.mpba.rastvdmy.service.CardService;
import api.mpba.rastvdmy.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.time.LocalDate;
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
public class CardControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    @MockBean
    private CardMapper cardMapper;

    private String jwtToken;

    @BeforeEach
    public void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

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
    public void callGetAccountCards_ShouldReturn_ListOfCards() throws Exception {
        // Given
        String bankName = "Bank1";
        UUID accountId = UUID.randomUUID();
        Card card1 = Card.builder()
                .id(UUID.randomUUID())
                .cardNumber("1234567890123456")
                .cvv("123")
                .pin("1234")
                .startDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusYears(3))
                .category(CardCategory.DEBIT)
                .type(CardType.VISA)
                .status(CardStatus.STATUS_CARD_DEFAULT)
                .build();
        Card card2 = Card.builder()
                .id(UUID.randomUUID())
                .cardNumber("1234567890123457")
                .cvv("124")
                .pin("1235")
                .startDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusYears(3))
                .category(CardCategory.DEBIT)
                .type(CardType.VISA)
                .status(CardStatus.STATUS_CARD_DEFAULT)
                .build();

        List<Card> cards = List.of(card1, card2);

        when(cardService.getAccountCards(eq(bankName), eq(accountId), any(HttpServletRequest.class))).thenReturn(cards);
        when(cardMapper.toResponse(any(CardRequest.class))).thenAnswer(invocation -> {
            CardRequest request = invocation.getArgument(0);
            return new CardResponse(
                    request.id(),
                    request.cardNumber(),
                    request.cvv(),
                    request.pin(),
                    request.startDate(),
                    request.expirationDate(),
                    request.cardCategory(),
                    request.cardType(),
                    request.cardStatus()
            );
        });

        // Then
        mockMvc.perform(
                        get("/api/v1/{bankName}/{accountId}/cards", bankName, accountId)
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].card_number").value(card1.getCardNumber()))
                .andExpect(jsonPath("$[0].cvv").value(card1.getCvv()))
                .andExpect(jsonPath("$[0].pin").value(card1.getPin()))
                .andExpect(jsonPath("$[0].start_date").value(card1.getStartDate().toString()))
                .andExpect(jsonPath("$[0].expiration_date").value(card1.getExpirationDate().toString()))
                .andExpect(jsonPath("$[0].card_category").value(card1.getCategory().toString()))
                .andExpect(jsonPath("$[0].card_type").value(card1.getType().toString()))
                .andExpect(jsonPath("$[0].card_status").value(card1.getStatus().toString()))
                .andExpect(jsonPath("$[1].card_number").value(card2.getCardNumber()))
                .andExpect(jsonPath("$[1].cvv").value(card2.getCvv()))
                .andExpect(jsonPath("$[1].pin").value(card2.getPin()))
                .andExpect(jsonPath("$[1].start_date").value(card2.getStartDate().toString()))
                .andExpect(jsonPath("$[1].expiration_date").value(card2.getExpirationDate().toString()))
                .andExpect(jsonPath("$[1].card_category").value(card2.getCategory().toString()))
                .andExpect(jsonPath("$[1].card_type").value(card2.getType().toString()))
                .andExpect(jsonPath("$[1].card_status").value(card2.getStatus().toString()));
    }

    @Test
    public void callGetAccountCardById_ShouldReturn_Card() throws Exception {
        // Given
        String bankName = "Bank1";
        UUID accountId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        Card card = Card.builder()
                .id(cardId)
                .cardNumber("1234567890123456")
                .cvv("123")
                .pin("1234")
                .startDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusYears(3))
                .category(CardCategory.DEBIT)
                .type(CardType.VISA)
                .status(CardStatus.STATUS_CARD_DEFAULT)
                .build();

        when(cardService.getAccountCardById(eq(bankName), eq(accountId), eq(cardId), any(HttpServletRequest.class), eq("visible"))).thenReturn(card);
        when(cardService.getAccountCardById(eq(bankName), eq(accountId), eq(cardId), any(HttpServletRequest.class), eq("non-visible"))).thenReturn(card);
        when(cardMapper.toResponse(any(CardRequest.class))).thenReturn(new CardResponse(
                card.getId(),
                card.getCardNumber(),
                card.getCvv(),
                card.getPin(),
                card.getStartDate(),
                card.getExpirationDate(),
                card.getCategory(),
                card.getType(),
                card.getStatus()
        ));

        // Test case for type "visible"
        mockMvc.perform(
                        get("/api/v1/{bankName}/{accountId}/cards/{cardId}", bankName, accountId, cardId)
                                .param("type", "visible")
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.card_number").value(card.getCardNumber()))
                .andExpect(jsonPath("$.cvv").value(card.getCvv()))
                .andExpect(jsonPath("$.pin").value(card.getPin()))
                .andExpect(jsonPath("$.start_date").value(card.getStartDate().toString()))
                .andExpect(jsonPath("$.expiration_date").value(card.getExpirationDate().toString()))
                .andExpect(jsonPath("$.card_category").value(card.getCategory().toString()))
                .andExpect(jsonPath("$.card_type").value(card.getType().toString()))
                .andExpect(jsonPath("$.card_status").value(card.getStatus().toString()));

        // Test case for type "non-visible"
        mockMvc.perform(
                        get("/api/v1/{bankName}/{accountId}/cards/{cardId}", bankName, accountId, cardId)
                                .param("type", "non-visible")
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.card_number").value(card.getCardNumber()))
                .andExpect(jsonPath("$.cvv").value(card.getCvv()))
                .andExpect(jsonPath("$.pin").value(card.getPin()))
                .andExpect(jsonPath("$.start_date").value(card.getStartDate().toString()))
                .andExpect(jsonPath("$.expiration_date").value(card.getExpirationDate().toString()))
                .andExpect(jsonPath("$.card_category").value(card.getCategory().toString()))
                .andExpect(jsonPath("$.card_type").value(card.getType().toString()))
                .andExpect(jsonPath("$.card_status").value(card.getStatus().toString()));
    }

    @Test
    public void callAddAccountCard_ShouldReturn_CreatedCard() throws Exception {
        // Given
        String bankName = "Bank1";
        UUID accountId = UUID.randomUUID();

        Card card = Card.builder()
                .id(UUID.randomUUID())
                .cardNumber("1234567890123456")
                .cvv("123")
                .pin("1234")
                .startDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusYears(3))
                .category(CardCategory.DEBIT)
                .type(CardType.VISA)
                .status(CardStatus.STATUS_CARD_DEFAULT)
                .build();

        CardRequest cardRequest = new CardRequest(
                UUID.randomUUID(),
                "1234567890123456",
                "123",
                "1234",
                LocalDate.now(),
                LocalDate.now().plusYears(3),
                CardCategory.DEBIT,
                CardType.VISA,
                CardStatus.STATUS_CARD_DEFAULT);

        when(cardService.addAccountCard(eq(bankName), eq(accountId), any(HttpServletRequest.class))).thenReturn(card);
        when(cardMapper.toResponse(any(CardRequest.class))).thenReturn(new CardResponse(
                card.getId(),
                card.getCardNumber(),
                card.getCvv(),
                card.getPin(),
                card.getStartDate(),
                card.getExpirationDate(),
                card.getCategory(), // Ensure category is mapped
                card.getType(),
                card.getStatus()
        ));

        // Then
        mockMvc.perform(
                        post("/api/v1/{bankName}/{accountId}/cards", bankName, accountId)
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cardRequest))
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.card_number").value(card.getCardNumber()))
                .andExpect(jsonPath("$.cvv").value(card.getCvv()))
                .andExpect(jsonPath("$.pin").value(card.getPin()))
                .andExpect(jsonPath("$.start_date").value(card.getStartDate().toString()))
                .andExpect(jsonPath("$.expiration_date").value(card.getExpirationDate().toString()))
                .andExpect(jsonPath("$.card_category").value(card.getCategory().toString()))
                .andExpect(jsonPath("$.card_type").value(card.getType().toString()))
                .andExpect(jsonPath("$.card_status").value(card.getStatus().toString()));
    }

    @Test
    public void callRemoveAccountCard_ShouldReturn_NoContent() throws Exception {
        // Given
        String bankName = "Bank1";
        UUID accountId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        // When
        doNothing().when(cardService).removeAccountCard(eq(bankName), eq(accountId), eq(cardId), any(HttpServletRequest.class));

        // Then
        mockMvc.perform(
                        delete("/api/v1/{bankName}/{accountId}/cards/{cardId}", bankName, accountId, cardId)
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());
    }
}