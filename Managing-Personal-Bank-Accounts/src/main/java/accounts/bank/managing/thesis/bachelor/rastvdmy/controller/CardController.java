package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;


import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.CardRefillRequest;
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

    @PostMapping(path = "/{id}") // Both admin and user
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Card> createCard(@PathVariable(value = "id") Long userId,
                                           @RequestBody CardRequest cardRequest) {
        LOG.debug("Creating card ...");
        return ResponseEntity.ok(cardService.createCard(userId, cardRequest.currency(), cardRequest.type()));
    }

    @PatchMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void cardRefill(@PathVariable(value = "id") Long cardId, @RequestBody CardRefillRequest cardRefillRequest) {
        LOG.debug("Refilling card ...");
        cardService.cardRefill(cardId, cardRefillRequest.pin(), cardRefillRequest.balance());
    }

    @PatchMapping(path = "/{id}/type")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void changeCardType
            (@PathVariable(value = "id") Long cardId, @RequestBody String cardType) {
        LOG.debug("Changing card type ...");
        cardService.changeCardType(cardId, cardType);
    }

    @PatchMapping(path = "/{id}/card-status")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void changeCardStatus(@PathVariable(value = "id") Long cardId, @RequestBody String cardStatus) {
        LOG.debug("Changing card status ...");
        cardService.changeCardStatus(cardId, cardStatus);
    }

    @DeleteMapping(path = "/{id}/from/{uid}") // Only admin
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable(value = "id") Long cardId, @PathVariable(value = "uid") Long userId) {
        LOG.debug("Deleting card {} from user {}...", cardId, userId);
        cardService.deleteCard(cardId, userId);
    }
}