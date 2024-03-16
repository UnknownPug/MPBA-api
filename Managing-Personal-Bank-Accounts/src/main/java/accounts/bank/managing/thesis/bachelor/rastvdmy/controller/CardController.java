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
        LOG.info("Getting all cards");
        return ResponseEntity.ok(cardService.getAllCards());
    }

    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Card> getCardById(@PathVariable(value = "id") Long cardId) {
        LOG.info("Getting card by id: " + cardId);
        return ResponseEntity.ok(cardService.getCardById(cardId));
    }

    @PostMapping(path = "/")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Card> createCard(@RequestBody CardRequest cardRequest) {
        if (cardRequest.cardNumber() == null || cardRequest.cvv() == null) {
            LOG.error("Card number is not valid.");
            return ResponseEntity.badRequest().build();
        }
        LOG.info("Creating card");
        return ResponseEntity.ok(cardService.createCard(cardRequest.cardNumber(), cardRequest.cvv()));
    }

    @PatchMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void changeCardBalance(@PathVariable(value = "id") Long cardId, @RequestBody CardRequest cardRequest) {
        LOG.info("Changing card balance");
        cardService.changeCardBalance(cardId, cardRequest.balance());
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable(value = "id") Long cardId) {
        LOG.info("Deleting card");
        cardService.deleteCard(cardId);
    }
}
