package api.mpba.rastvdmy.controller;


import api.mpba.rastvdmy.controller.mapper.CardMapper;
import api.mpba.rastvdmy.dto.request.CardRequest;
import api.mpba.rastvdmy.dto.response.CardResponse;
import api.mpba.rastvdmy.entity.Card;
import api.mpba.rastvdmy.service.CardService;
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
import java.util.stream.Collectors;

@Slf4j
@RestController
@PreAuthorize("hasRole('ROLE_DEFAULT')")
@RequestMapping(path = "/api/v1/{bankName}/{number}/cards")
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
    public ResponseEntity<List<CardResponse>> getAccountCards(@PathVariable("bankName") String bankName,
                                                              @PathVariable("number") String accountNumber,
                                                              HttpServletRequest request) {
        logInfo("Getting all cards ...");
        List<Card> cards = cardService.getAccountCards(bankName, accountNumber, request);
        List<CardResponse> cardResponses = cards.stream()
                .map(card -> cardMapper.toResponse(new CardRequest(
                        card.getCardNumber(),
                        card.getCvv(),
                        card.getPin(),
                        card.getStartDate(),
                        card.getExpirationDate(),
                        card.getCategory(),
                        card.getType(),
                        card.getStatus())
                )).collect(Collectors.toList());
        return ResponseEntity.ok(cardResponses);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{cardNumber}", produces = "application/json")
    public ResponseEntity<CardResponse> getCard(@PathVariable("bankName") String bankName,
                                                @PathVariable("number") String accountNumber,
                                                @PathVariable("cardNumber") String cardNumber,
                                                HttpServletRequest request) {
        logInfo("Getting card ...");
        Card card = cardService.getAccountCardByNumber(bankName, accountNumber, cardNumber, request);
        CardResponse cardResponse = cardMapper.toResponse(new CardRequest(
                card.getCardNumber(),
                card.getCvv(),
                card.getPin(),
                card.getStartDate(),
                card.getExpirationDate(),
                card.getCategory(),
                card.getType(),
                card.getStatus()));
        return ResponseEntity.ok(cardResponse);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = "application/json")
    public ResponseEntity<CardResponse> addAccountCard(@PathVariable("bankName") String bankName,
                                                       @PathVariable("number") String accountNumber,
                                                       HttpServletRequest request) throws Exception {
        logInfo("Creating new card ...");
        Card card = cardService.addAccountCard(bankName, accountNumber, request);
        CardResponse cardResponse = cardMapper.toResponse(new CardRequest(
                card.getCardNumber(),
                card.getCvv(),
                card.getPin(),
                card.getStartDate(),
                card.getExpirationDate(),
                card.getCategory(),
                card.getType(),
                card.getStatus()));

        return ResponseEntity.status(HttpStatus.CREATED).body(cardResponse);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{cardNumber}")
    public ResponseEntity<Void> removeAccountCard(
            @PathVariable(name = "bankName") String bankName,
            @PathVariable(name = "number") String accountNumber,
            @PathVariable(name = "cardNumber") String cardNumber,
            HttpServletRequest request) {
        logInfo("Removing card...", cardNumber);
        cardService.removeAccountCard(bankName, accountNumber, cardNumber, request);
        return ResponseEntity.noContent().build();
    }

    private void logInfo(String message, Object... args) {
        LOG.info(message, args);
    }
}