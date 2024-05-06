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

/**
 * This class is responsible for handling card related requests.
 * It provides endpoints for getting all cards, filtering cards, getting a card by id or card number,
 * updating a card status, creating a card, refilling a card, changing a card type, and deleting a card.
 */
@Slf4j
@RestController
@RequestMapping(path = "/card")
public class CardController {
    private static final Logger LOG = LoggerFactory.getLogger(CardController.class);
    private final CardService cardService;

    /**
     * Constructor for the CardController.
     *
     * @param cardService The service to handle card operations.
     */
    @Autowired
    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    /**
     * This method is used to get all cards.
     *
     * @return A list of all cards.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public ResponseEntity<List<Card>> getCards() {
        LOG.info("Getting all cards ...");
        return ResponseEntity.ok(cardService.getAllCards());
    }

    /**
     * This method is used to filter cards.
     *
     * @param page The page number.
     * @param size The size of the page.
     * @param sort The sort order.
     * @return A page of filtered cards.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/filter")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<Page<Card>> filterCards(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "asc") String sort) {
        LOG.info("Filtering cards ...");
        switch (sort.toLowerCase()) {
            case "asc":
                LOG.info("Sorting cards by balance in ascending order ...");
                Pageable pageableAsc = PageRequest.of(page, size, Sort.by("cardNumber").ascending());
                return ResponseEntity.ok(cardService.filterAndSortCards(pageableAsc));
            case "desc":
                LOG.info("Sorting cards by balance in descending order ...");
                Pageable pageableDesc = PageRequest.of(page, size, Sort.by("cardNumber").descending());
                return ResponseEntity.ok(cardService.filterAndSortCards(pageableDesc));
            default:
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid sort option. Use 'asc' or 'desc'.");
        }
    }

    /**
     * This method is used to get a card by id.
     *
     * @param cardId The id of the card.
     * @return The card with the given id.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public ResponseEntity<Card> getCardById(@PathVariable(value = "id") Long cardId) {
        LOG.info("Getting card id {} ...", cardId);
        return ResponseEntity.ok(cardService.getCardById(cardId));
    }

    /**
     * This method is used to get a card by card number.
     *
     * @param request The request containing the card number.
     * @return The card with the given card number.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/number")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public ResponseEntity<Card> getCardByCardNumber(@RequestBody CardRequest request) {
        LOG.info("Getting card by card number {} ...", request.cardNumber());
        return ResponseEntity.ok(cardService.getCardByCardNumber(request.cardNumber()));
    }

    /**
     * This method is used to update a card status.
     *
     * @param id The id of the card.
     */
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(path = "/{id}/status")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public void updateUserCardStatus(@PathVariable(value = "id") Long id) {
        LOG.info("Getting card status ...");
        cardService.updateCardStatus(id);
    }

    /**
     * This method is used to create a card.
     *
     * @param userId      The id of the user.
     * @param cardRequest The request containing the currency and type of the card.
     * @return The created card.
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public ResponseEntity<Card> createCard(@PathVariable(value = "id") Long userId,
                                           @RequestBody CardRequest cardRequest) {
        LOG.info("Creating card ...");
        return ResponseEntity.ok(cardService.createCard(userId, cardRequest.currency(), cardRequest.type()));
    }

    /**
     * This method is used to refill a card.
     *
     * @param cardId  The id of the card.
     * @param request The request containing the pin and balance of the card.
     */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public void cardRefill(@PathVariable(value = "id") Long cardId, @RequestBody CardRequest request) {
        LOG.info("Refilling card ...");
        cardService.cardRefill(cardId, request.pin(), request.balance());
    }

    /**
     * This method is used to change a card type.
     *
     * @param cardId The id of the card.
     */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}/type")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public void changeCardType(@PathVariable(value = "id") Long cardId) {
        LOG.info("Changing card type ...");
        cardService.changeCardType(cardId);
    }

    /**
     * This method is used to delete a card.
     *
     * @param cardId The id of the card.
     * @param userId The id of the user.
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{id}/from/{uid}") // Only admin
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteCard(@PathVariable(value = "id") Long cardId, @PathVariable(value = "uid") Long userId) {
        LOG.info("Deleting card {} from user {}...", cardId, userId);
        cardService.deleteCard(cardId, userId);
    }
}