package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Card;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CardService {

    private final CardRepository cardRepository;

    @Autowired
    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    public Card getCardById(Long cardId) {
        return cardRepository.findById(cardId).orElseThrow(
                () -> new RuntimeException("Card with id: " + cardId + " not found")
        );
    }

    public Card createCard() {
        // TODO: complete this method
        return null;
    }

    public void cardRefill(Long cardId, Integer pin, BigDecimal balance) {
        // TODO: complete this method
        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new RuntimeException("Card with id: " + cardId + " not found")
        );
        if (card.getPin().equals(pin)) {
            card.setBalance(balance);
            card.setRecipientTime(LocalDateTime.now());
            cardRepository.save(card);
        } else {
            throw new RuntimeException("Wrong pin");
        }
    }

    public void deleteCard(Long cardId) {
        // TODO: complete this method
    }
}
