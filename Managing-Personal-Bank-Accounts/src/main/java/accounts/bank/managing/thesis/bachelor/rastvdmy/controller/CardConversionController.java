package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.CardConversionRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.CardConversion;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.CardConversionService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
        LOG.info("Getting all card conversions ...");
        return ResponseEntity.ok(cardConversionService.getAllCardConversions());
    }

    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CardConversion> getCardConversionById(@PathVariable("id") Long conversionId) {
        LOG.info("Getting card conversion id: {} ...", conversionId);
        return ResponseEntity.ok(cardConversionService.getCardConversionById(conversionId));
    }

    @PostMapping(path = "/")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CardConversion> changeCurrency(@RequestParam(value = "type") String type,
                                                         @RequestBody CardConversionRequest cardConversionRequest) {
        LOG.info("Creating card conversion ...");
        if (type != null) {
            return ResponseEntity.ok(
                    cardConversionService.changeCurrency(type, cardConversionRequest.targetAmount())
            );
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Type not found. Use 'type' parameter");
        }
    }

    @PatchMapping(path = "/commission")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateCommission(@RequestParam(value = "type") String type,
                                 @RequestParam(value = "newCommission") BigDecimal newCommission) {
        if (type != null && newCommission != null) {
            LOG.info("Updating commission for currency: {} ...", type);
            cardConversionService.updateCommission(type, newCommission);
        }
        throw new ApplicationException(HttpStatus.NOT_FOUND,
                "Currency or new commission not found. Use 'type' and 'newCommission' parameters");
    }
}
