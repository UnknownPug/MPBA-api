package api.mpba.rastvdmy.controller;

import api.mpba.rastvdmy.controller.mapper.CardMapper;
import api.mpba.rastvdmy.dto.request.CardRequest;
import api.mpba.rastvdmy.dto.response.CardResponse;
import api.mpba.rastvdmy.entity.Card;
import api.mpba.rastvdmy.service.CardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller for managing bank account cards.
 * <p>
 * This controller provides endpoints for users to manage their cards associated with specific bank accounts,
 * including retrieving all cards, adding new cards, getting a card by ID, and removing cards.
 * It uses the {@link CardService} for business logic and {@link CardMapper} for mapping between request
 * and response objects.
 * </p>
 */
@Slf4j
@RestController
@PreAuthorize("hasRole('ROLE_DEFAULT')")
@RequestMapping(path = "/api/v1/{bankName}/{accountId}/cards")
public class CardController {
    private final CardMapper cardMapper;
    private final CardService cardService;

    /**
     * Constructor for CardController.
     *
     * @param cardMapper  The {@link CardMapper} to be used.
     * @param cardService The {@link CardService} to be used.
     */
    public CardController(CardMapper cardMapper, CardService cardService) {
        this.cardService = cardService;
        this.cardMapper = cardMapper;
    }

    /**
     * Retrieves all cards associated with a specific bank account.
     *
     * @param bankName  The name of the bank.
     * @param accountId The UUID of the account.
     * @param request   The HTTP servlet request containing user information.
     * @return A {@link ResponseEntity} containing a list of {@link CardResponse}.
     */
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<CardResponse>> getAccountCards(@PathVariable("bankName") String bankName,
                                                              @PathVariable("accountId") UUID accountId,
                                                              HttpServletRequest request) {
        logInfo("Getting all cards ...");
        List<Card> cards = cardService.getAccountCards(bankName, accountId, request);
        List<CardResponse> cardResponses = cards.stream()
                .map(card -> cardMapper.toResponse(convertToCardRequest(card)))
                .collect(Collectors.toList());
        return ResponseEntity.ok(cardResponses);
    }

    /**
     * Retrieves a specific card by its ID.
     *
     * @param bankName  The name of the bank.
     * @param accountId The UUID of the account.
     * @param cardId    The UUID of the card to retrieve.
     * @param type      The visibility type of the card (default is "non-visible").
     * @param request   The HTTP servlet request containing user information.
     * @return A {@link ResponseEntity} containing the requested {@link CardResponse}.
     */
    @GetMapping(path = "/{cardId}", produces = "application/json")
    public ResponseEntity<CardResponse> getAccountCardById(@PathVariable("bankName") String bankName,
                                                           @PathVariable("accountId") UUID accountId,
                                                           @PathVariable("cardId") UUID cardId,
                                                           @RequestParam(value = "type",
                                                                   defaultValue = "non-visible") String type,
                                                           HttpServletRequest request) {
        logInfo("Getting card ...");
        Card card = cardService.getAccountCardById(bankName, accountId, cardId, request, type);
        CardResponse cardResponse = cardMapper.toResponse(convertToCardRequest(card));
        return ResponseEntity.ok(cardResponse);
    }

    /**
     * Adds a new card to a specific bank account.
     *
     * @param bankName  The name of the bank.
     * @param accountId The UUID of the account.
     * @param request   The HTTP servlet request containing user information.
     * @return A {@link ResponseEntity} containing the created {@link CardResponse}.
     * @throws Exception If there is an error while adding the card.
     */
    @PostMapping(produces = "application/json")
    public ResponseEntity<CardResponse> addAccountCard(@PathVariable("bankName") String bankName,
                                                       @PathVariable("accountId") UUID accountId,
                                                       HttpServletRequest request) throws Exception {
        logInfo("Creating new card ...");
        Card card = cardService.addAccountCard(bankName, accountId, request);
        CardResponse cardResponse = cardMapper.toResponse(convertToCardRequest(card));
        return ResponseEntity.status(HttpStatus.CREATED).body(cardResponse);
    }

    /**
     * Converts a {@link Card} to a {@link CardRequest}.
     *
     * @param card The card to convert.
     * @return The converted card.
     */
    private static CardRequest convertToCardRequest(Card card) {
        return new CardRequest(
                card.getId(),
                card.getCardNumber(),
                card.getCvv(),
                card.getPin(),
                card.getStartDate(),
                card.getExpirationDate(),
                card.getCategory(),
                card.getType(),
                card.getStatus());
    }

    /**
     * Removes a card from a specific bank account.
     *
     * @param bankName  The name of the bank.
     * @param accountId The UUID of the account.
     * @param cardId    The UUID of the card to remove.
     * @param request   The HTTP servlet request containing user information.
     * @return A {@link ResponseEntity} with no content.
     */
    @DeleteMapping(path = "/{cardId}")
    public ResponseEntity<Void> removeAccountCard(
            @PathVariable(name = "bankName") String bankName,
            @PathVariable(name = "accountId") UUID accountId,
            @PathVariable(name = "cardId") UUID cardId,
            HttpServletRequest request) {
        logInfo("Removing card...");
        cardService.removeAccountCard(bankName, accountId, cardId, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Logs informational messages to the console.
     *
     * @param message The message to log.
     * @param args    Optional arguments to format the message.
     */
    private void logInfo(String message, Object... args) {
        log.info(message, args);
    }
}