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

    public Card createCard(Integer cardNumber, Integer cvv) {
        Card card = new Card();
        card.setCardNumber(cardNumber);
        card.setCvv(cvv);
        card.setBalance(BigDecimal.ZERO);
        card.setExpirationDate(LocalDateTime.now().plusYears(5));
        return cardRepository.save(card);
    }

    public void changeCardBalance(Long cardId, BigDecimal balance) {
        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new RuntimeException("Card with id: " + cardId + " not found")
        );
        card.setBalance(balance);
        cardRepository.save(card);
    }

    public void deleteCard(Long cardId) {
        cardRepository.findById(cardId).orElseThrow(
                () -> new RuntimeException("Card with id: " + cardId + " not found")
        );
        cardRepository.deleteById(cardId);
    }
}
