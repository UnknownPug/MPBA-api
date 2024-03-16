package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.CardConversionRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.CardConversion;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.CardConversionService;
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
@RequestMapping("/conversion")
public class CardConversionController {

    private static final Logger LOG = LoggerFactory.getLogger(CardConversionController.class);
    private final CardConversionService cardConversionService;

    @Autowired
    public CardConversionController(CardConversionService cardConversionService) {
        this.cardConversionService = cardConversionService;
    }

    @GetMapping(path = "/")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<CardConversion>> getAllCardConversions() {
        LOG.info("Getting all card conversions");
        return ResponseEntity.ok(cardConversionService.getAllCardConversions());
    }

    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CardConversion> getCardConversionById(@PathVariable("id") Long conversionId) {
        LOG.info("Getting card conversion by id: {}", conversionId);
        return ResponseEntity.ok(cardConversionService.getCardConversionById(conversionId));
    }

    @PostMapping(path = "/")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CardConversion> createCardConversion(@RequestBody CardConversionRequest cardConversionRequest) {
        LOG.info("Creating card conversion: {}", cardConversionRequest);
        return ResponseEntity.ok(cardConversionService.createCardConversion(
                cardConversionRequest.prevAmount(),
                cardConversionRequest.targetAmount(),
                cardConversionRequest.commission()));
    }

    @PutMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void updateCardConversion(@PathVariable("id") Long conversionId,
                                                              @RequestBody CardConversionRequest cardConversionRequest) {
        LOG.info("Updating card conversion with id: {}", conversionId);
        cardConversionService.updateCardConversion(
                conversionId,
                cardConversionRequest.prevAmount(),
                cardConversionRequest.targetAmount(),
                cardConversionRequest.commission());
    }

    @PatchMapping(path = "/{id}/prev/amount")
    @ResponseStatus(HttpStatus.OK)
    public void updatePrevAmount(@PathVariable("id") Long conversionId,
                                                           @RequestBody CardConversionRequest cardConversionRequest) {
        LOG.info("Updating previous amount of card conversion with id: {}", conversionId);
        cardConversionService.updatePrevAmount(
                conversionId,
                cardConversionRequest.prevAmount());
    }

    @PatchMapping(path = "/{id}/target/amount")
    @ResponseStatus(HttpStatus.OK)
    public void updateTargetAmount(@PathVariable("id") Long conversionId,
                                                             @RequestBody CardConversionRequest cardConversionRequest) {
        LOG.info("Updating target amount of card conversion with id: {}", conversionId);
        cardConversionService.updateTargetAmount(
                conversionId,
                cardConversionRequest.targetAmount());
    }

    @PatchMapping(path = "/{id}/commission")
    @ResponseStatus(HttpStatus.OK)
    public void updateCommission(@PathVariable("id") Long conversionId,
                                                           @RequestBody CardConversionRequest cardConversionRequest) {
        LOG.info("Updating commission of card conversion with id: {}", conversionId);
        cardConversionService.updateCommission(
                conversionId,
                cardConversionRequest.commission());
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteCardConversion(@PathVariable("id") Long conversionId) {
        LOG.info("Deleting card conversion with id: {}", conversionId);
        cardConversionService.deleteCardConversion(conversionId);
        return ResponseEntity.noContent().build();
    }

}
