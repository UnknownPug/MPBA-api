package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;


import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.CardRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Card;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
//    @PreAuthorize("hasRole('MODERATOR')")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Card>> getCards() {
        LOG.debug("Getting all cards ...");
        return ResponseEntity.ok(cardService.getAllCards());
    }

    @GetMapping(path = "/filter")
    @ResponseStatus(HttpStatus.OK)
//    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<Page<Card>> filterCards(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "asc") String sort) {
        LOG.debug("Filtering cards ...");
        switch (sort.toLowerCase()) {
            case "asc":
                LOG.debug("Sorting cards by balance in ascending order ...");
                Pageable pageableAsc = PageRequest.of(page, size, Sort.by("cardNumber").ascending());
                return ResponseEntity.ok(cardService.filterAndSortCards(pageableAsc));
            case "desc":
                LOG.debug("Sorting cards by balance in descending order ...");
                Pageable pageableDesc = PageRequest.of(page, size, Sort.by("cardNumber").descending());
                return ResponseEntity.ok(cardService.filterAndSortCards(pageableDesc));
            default:
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid sort option. Use 'asc' or 'desc'.");
        }
    }

    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
//    @PreAuthorize("hasAnyRole('MODERATOR', 'USER')")
    public ResponseEntity<Card> getCardById(@PathVariable(value = "id") Long cardId) {
        LOG.debug("Getting card id {} ...", cardId);
        return ResponseEntity.ok(cardService.getCardById(cardId));
    }

    @GetMapping(path = "/number")
    @ResponseStatus(HttpStatus.OK)
//    @PreAuthorize("hasAnyRole('MODERATOR', 'USER')")
    public ResponseEntity<Card> getCardByCardNumber(@RequestBody CardRequest request) {
        LOG.debug("Getting card by card number {} ...", request.cardNumber());
        return ResponseEntity.ok(cardService.getCardByCardNumber(request.cardNumber()));
    }

    @PatchMapping(path = "/{id}/status")
    @ResponseStatus(HttpStatus.OK)
//    @PreAuthorize("hasRole('MODERATOR')")
    public void updateUserCardStatus(@PathVariable(value = "id") Long id) {
        LOG.debug("Getting card status ...");
        cardService.updateCardStatus(id);
    }

    @PostMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.CREATED)
//    @PreAuthorize("hasAnyRole('MODERATOR', 'USER')")
    public ResponseEntity<Card> createCard(@PathVariable(value = "id") Long userId,
                                           @RequestBody CardRequest cardRequest) {
        LOG.debug("Creating card ...");
        return ResponseEntity.ok(cardService.createCard(userId, cardRequest.currency(), cardRequest.type()));
    }

    @PatchMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void cardRefill(@PathVariable(value = "id") Long cardId, @RequestBody CardRequest request) {
        LOG.debug("Refilling card ...");
        cardService.cardRefill(cardId, request.pin(), request.balance());
    }

    @PatchMapping(path = "/{id}/type")
    @ResponseStatus(HttpStatus.ACCEPTED)
//    @PreAuthorize("hasRole('MODERATOR')")
    public void changeCardType
            (@PathVariable(value = "id") Long cardId, @RequestBody CardRequest request) {
        LOG.debug("Changing card type ...");
        cardService.changeCardType(cardId, request.type());
    }

    @DeleteMapping(path = "/{id}/from/{uid}") // Only admin
    @ResponseStatus(HttpStatus.NO_CONTENT)
//    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public void deleteCard(@PathVariable(value = "id") Long cardId, @PathVariable(value = "uid") Long userId) {
        LOG.debug("Deleting card {} from user {}...", cardId, userId);
        cardService.deleteCard(cardId, userId);
    }
}