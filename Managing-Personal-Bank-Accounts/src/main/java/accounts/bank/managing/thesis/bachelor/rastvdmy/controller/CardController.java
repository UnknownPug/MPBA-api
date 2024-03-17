package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;


import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.CardRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Card;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.CardService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/card")
public class CardController {

    private static final Logger LOG = LoggerFactory.getLogger(CardController.class);
    private final CardService cardService;

    @Autowired
    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping(path = "/")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Card>> getCards() {
        LOG.debug("Getting all cards ...");
        return ResponseEntity.ok(cardService.getAllCards());
    }

    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Card> getCardById(@PathVariable(value = "id") Long cardId) {
        LOG.debug("Getting card id {} ...", cardId);
        return ResponseEntity.ok(cardService.getCardById(cardId));
    }

    @PostMapping(path = "/") // Both admin and user
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Card> createCard() {
        LOG.debug("Creating card...");
        return ResponseEntity.ok(cardService.createCard());
    }

    @PatchMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void cardRefill(@PathVariable(value = "id") Long cardId, @RequestBody CardRequest cardRequest) {
        LOG.debug("Refilling card ...");
        cardService.cardRefill(cardId, cardRequest.pin(), cardRequest.balance());
    }

    @DeleteMapping(path = "/{id}") // Only admin
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable(value = "id") Long cardId) {
        LOG.debug("Deleting card {} ...", cardId);
        cardService.deleteCard(cardId);
    }
}
