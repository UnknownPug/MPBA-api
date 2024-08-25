package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;


import accounts.bank.managing.thesis.bachelor.rastvdmy.controller.mapper.CardMapper;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.CardRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.response.CardResponse;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Card;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.CardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping(path = "/api/v1/{accountId}/cards")
public class CardController {
    private static final Logger LOG = LoggerFactory.getLogger(CardController.class);
    private final CardMapper cardMapper;
    private final CardService cardService;

    @Autowired
    public CardController(CardMapper cardMapper, CardService cardService) {
        this.cardService = cardService;
        this.cardMapper = cardMapper;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<CardResponse>> getAccountCards(@PathVariable("accountId") UUID id,
                                                              HttpServletRequest request) {
        logInfo("Getting all cards ...");
        List<Card> cards = cardService.getAccountCards(id, request);
        List<CardResponse> cardResponses = cards.stream()
                .map(card -> cardMapper.toResponse(new CardRequest(
                        card.getCardNumber(),
                        card.getCvv(),
                        card.getPin(),
                        card.getStartDate(),
                        card.getExpirationDate())
                )).collect(Collectors.toList());
        return ResponseEntity.ok(cardResponses);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<CardResponse> getCard(@PathVariable("accountId") UUID accountId,
                                                @PathVariable(name = "id") UUID cardId,
                                                HttpServletRequest request) {
        logInfo("Getting card ...");
        Card card = cardService.getAccountCardById(accountId, cardId, request);
        CardResponse cardResponse = cardMapper.toResponse(new CardRequest(
                card.getCardNumber(),
                card.getCvv(),
                card.getPin(),
                card.getStartDate(),
                card.getExpirationDate()));
        return ResponseEntity.ok(cardResponse);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<CardResponse> addAccountCard(@PathVariable("accountId") UUID id, HttpServletRequest request,
                                                @Valid @RequestBody CardRequest cardRequest) throws Exception {
        logInfo("Creating new card ...");
        Card card = cardService.addAccountCard(id, request, cardRequest);
        CardResponse cardResponse = cardMapper.toResponse(new CardRequest(
                card.getCardNumber(),
                card.getCvv(),
                card.getPin(),
                card.getStartDate(),
                card.getExpirationDate()));
        return ResponseEntity.status(HttpStatus.CREATED).body(cardResponse);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> updateAccountCardStatus(@PathVariable("accountId") UUID accountId,
                                                 @PathVariable(name = "id") UUID cardId,
                                                 HttpServletRequest request) {
        logInfo("Updating card status...");
        cardService.updateAccountCardStatus(accountId, cardId, request);
        return ResponseEntity.noContent().build();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> removeAccountCard(
            @PathVariable("accountId") UUID accountId,
            @PathVariable(name = "id") UUID cardId,
            HttpServletRequest request) {
        logInfo("Removing card...", cardId);
        cardService.removeAccountCard(accountId, cardId, request);
        return ResponseEntity.noContent().build();
    }

    private void logInfo(String message, Object... args) {
        LOG.info(message, args);
    }
}