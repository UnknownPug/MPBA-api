package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.CardConversion;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CardConversionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CardConversionService {

    private final CardConversionRepository cardConversionRepository;

    @Autowired
    public CardConversionService(CardConversionRepository cardConversionRepository) {
        this.cardConversionRepository = cardConversionRepository;
    }

    public List<CardConversion> getAllCardConversions() {
        return cardConversionRepository.findAll();
    }

    public CardConversion getCardConversionById(Long conversionId) {
        return cardConversionRepository.findById(conversionId).orElseThrow(
                () -> new RuntimeException("Card conversion with id: " + conversionId + " not found")
        );
    }

    public CardConversion changeCurrency(String type, BigDecimal bigDecimal) {
        // TODO: complete this method
        // Money is in a bank account (non-deposit) - can be converted to any available currency
        return null;
    }

    public void updateCommission(String type, BigDecimal newCommission) {
        // TODO: complete this method
        // Percentage for services (withdrawn from the card)
    }
}
