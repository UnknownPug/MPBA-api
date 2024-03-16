package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.CardConversion;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CardConversionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    public CardConversion createCardConversion(BigDecimal prevAmount,
                                               BigDecimal targetAmount,
                                               BigDecimal commission) {
        CardConversion cardConversion = new CardConversion();
        if (prevAmount.compareTo(BigDecimal.ZERO) <= 0  || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Previous amount must be greater than 0");
        }
        if (commission.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Commission must be greater than or equal to 0");
        }
        cardConversion.setPrevAmount(prevAmount);
        cardConversion.setTargetAmount(targetAmount);
        cardConversion.setCommission(commission);
        cardConversion.setDateTime(LocalDateTime.now());
        return cardConversionRepository.save(cardConversion);
    }

    public void updateCardConversion(Long conversionId, BigDecimal prevAmount, BigDecimal targetAmount, BigDecimal commission) {
        CardConversion cardConversion = cardConversionRepository.findById(conversionId).orElseThrow(
                () -> new RuntimeException("Card conversion with id: " + conversionId + " not found")
        );
        if (prevAmount.compareTo(BigDecimal.ZERO) <= 0  || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Previous amount must be greater than 0");
        }
        if (commission.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Commission must be greater than or equal to 0");
        }
        cardConversion.setPrevAmount(prevAmount);
        cardConversion.setTargetAmount(targetAmount);
        cardConversion.setCommission(commission);
        cardConversion.setDateTime(LocalDateTime.now());
        cardConversionRepository.save(cardConversion);
    }

    public void updatePrevAmount(Long conversionId, BigDecimal prevAmount) {
        CardConversion cardConversion = cardConversionRepository.findById(conversionId).orElseThrow(
                () -> new RuntimeException("Card conversion with id: " + conversionId + " not found")
        );
        if (prevAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Previous amount must be greater than 0");
        }
        cardConversion.setPrevAmount(prevAmount);
        cardConversion.setDateTime(LocalDateTime.now());
        cardConversionRepository.save(cardConversion);
    }

    public void updateTargetAmount(Long conversionId, BigDecimal targetAmount) {
        CardConversion cardConversion = cardConversionRepository.findById(conversionId).orElseThrow(
                () -> new RuntimeException("Card conversion with id: " + conversionId + " not found")
        );
        if (targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Target amount must be greater than 0");
        }
        cardConversion.setTargetAmount(targetAmount);
        cardConversion.setDateTime(LocalDateTime.now());
        cardConversionRepository.save(cardConversion);
    }

    public void updateCommission(Long conversionId, BigDecimal commission) {
        CardConversion cardConversion = cardConversionRepository.findById(conversionId).orElseThrow(
                () -> new RuntimeException("Card conversion with id: " + conversionId + " not found")
        );
        if (commission.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Commission must be greater than or equal to 0");
        }
        cardConversion.setCommission(commission);
        cardConversion.setDateTime(LocalDateTime.now());
        cardConversionRepository.save(cardConversion);
    }

    public void deleteCardConversion(Long conversionId) {
        cardConversionRepository.findById(conversionId).orElseThrow(
                () -> new RuntimeException("Card conversion with id: " + conversionId + " not found")
        );
        cardConversionRepository.deleteById(conversionId);
    }
}
