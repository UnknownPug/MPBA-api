package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;


import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.CardRefillRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.CardRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Card;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.CardService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @GetMapping(path = "/filter")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Page<Card>> filterCards(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "asc") String sort) {
        LOG.debug("Filtering cards ...");
        if (sort.equalsIgnoreCase("asc")) {
            LOG.debug("Sorting cards by balance in ascending order ...");
            Pageable pageable = PageRequest.of(page, size, Sort.by("balance").ascending());
            return ResponseEntity.ok(cardService.filterAndSortCards(pageable));
        } else if (sort.equalsIgnoreCase("desc")) {
            LOG.debug("Sorting cards by balance in descending order ...");
            Pageable pageable = PageRequest.of(page, size, Sort.by("balance").descending());
            return ResponseEntity.ok(cardService.filterAndSortCards(pageable));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Card> getCardById(@PathVariable(value = "id") Long cardId) {
        LOG.debug("Getting card id {} ...", cardId);
        return ResponseEntity.ok(cardService.getCardById(cardId));
    }

    @GetMapping(path = "/card")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Card> getCardByCardNumber(@RequestBody String cardNumber) {
        LOG.debug("Getting card by card number {} ...", cardNumber);
        return ResponseEntity.ok(cardService.getCardByCardNumber(cardNumber));
    }

    @GetMapping(path = "/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public void updateUserCardStatus(@PathVariable(value = "id") Long id) {
        LOG.debug("Getting card status ...");
        cardService.updateCardStatus(id);
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

    @DeleteMapping(path = "/{id}/from/{uid}") // Only admin
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable(value = "id") Long cardId, @PathVariable(value = "uid") Long userId) {
        LOG.debug("Deleting card {} from user {}...", cardId, userId);
        cardService.deleteCard(cardId, userId);
    }
}