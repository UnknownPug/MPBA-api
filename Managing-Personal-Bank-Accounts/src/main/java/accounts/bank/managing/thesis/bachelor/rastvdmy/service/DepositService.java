package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.CardStatus;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.component.Generator;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Card;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Currency;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Deposit;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CardRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CurrencyDataRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.DepositRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DepositService {
    private final DepositRepository depositRepository;
    private final Generator generator;
    private final CurrencyDataRepository currencyRepository;
    private final CardRepository cardRepository;

    @Autowired
    public DepositService(DepositRepository depositRepository, Generator generator, CurrencyDataRepository currencyRepository, CardRepository cardRepository) {
        this.depositRepository = depositRepository;
        this.generator = generator;
        this.currencyRepository = currencyRepository;
        this.cardRepository = cardRepository;
    }

    @Cacheable(value = "deposits")
    public List<Deposit> getAllDeposits() {
        return depositRepository.findAll();
    }

    @Cacheable(value = "deposits")
    public Page<Deposit> filterAndSortDeposits(Pageable pageable) {
        return depositRepository.findAll(pageable);
    }

    @Cacheable(value = "deposits", key = "#id")
    public Deposit getDepositById(Long id) {
        return depositRepository.findById(id).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Deposit is not found.")
        );
    }

    @CacheEvict(value = {"deposits", "cards"}, allEntries = true)
    public Deposit openDeposit(String cardNumber, BigDecimal depositAmount, String description, Currency currency) {
        Card card = getUserCard(cardNumber, depositAmount);
        if (depositRepository.existsByCardDeposit(card)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "A deposit for this card already exists.");
        }
        Deposit deposit = new Deposit();
        deposit.setCurrency(currency);
        deposit.setDepositCard(generator.generateAccountNumber());
        deposit.setDepositAmount(convertCurrency(deposit, depositAmount));
        deposit.setCardDeposit(card);
        deposit.setDescription(description);
        deposit.setStartDate(LocalDateTime.now());
        deposit.setExpirationDate(deposit.getStartDate().plusYears(1));
        String referenceNumber;
        do {
            referenceNumber = generator.generateReferenceNumber();
        } while (depositRepository.existsByReferenceNumber(referenceNumber));
        deposit.setReferenceNumber(referenceNumber);
        card.setBalance(card.getBalance().subtract(depositAmount));
        cardRepository.save(card);

        return depositRepository.save(deposit);
    }

    private BigDecimal convertCurrency(Deposit deposit, BigDecimal depositAmount) {
        return switch (deposit.getCurrency()) {
            case USD -> depositAmount.multiply(
                    BigDecimal.valueOf(currencyRepository.findByCurrency(Currency.USD.toString()).getRate())
            );
            case EUR -> depositAmount.multiply(
                    BigDecimal.valueOf(currencyRepository.findByCurrency(Currency.EUR.toString()).getRate())
            );
            case CZK -> depositAmount.multiply(
                    BigDecimal.valueOf(currencyRepository.findByCurrency(Currency.CZK.toString()).getRate())
            );
            case UAH -> depositAmount.multiply(
                    BigDecimal.valueOf(currencyRepository.findByCurrency(Currency.UAH.toString()).getRate())
            );
            case PLN -> depositAmount.multiply(
                    BigDecimal.valueOf(currencyRepository.findByCurrency(Currency.PLN.toString()).getRate())
            );
        };
    }

    // The deposit cannot be renewed before the end of the deposit period
    // (with the condition of not improving/deteriorating).
    @CacheEvict(value = {"deposits", "cards"}, allEntries = true)
    public void updateDeposit(Long depositId, String cardNumber, String description, BigDecimal newAmount, Currency currency) {
        Deposit deposit = depositRepository.findById(depositId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Deposit is not valid.")
        );
        Card card = getUserCard(cardNumber, newAmount);
        if (deposit.getCardDeposit().equals(card)) {
            deleteDeposit(depositId); // Delete the deposit after the money has been returned to the card
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Card must be the same as the card that was used for the deposit.");
        }
        openDeposit(cardNumber, newAmount, description, currency); // Open a new deposit
    }

    private Card getUserCard(String cardNumber, BigDecimal newAmount) {
        Card card = cardRepository.findByCardNumber(cardNumber);
        if (card == null) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Card is not found.");
        }
        if (card.getStatus() == CardStatus.STATUS_CARD_BLOCKED) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Operation is unavailable. Card is blocked.");
        }
        if (card.getBalance().compareTo(newAmount) < 0) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Not enough money on the card.");
        }
        return card;
    }

    @CacheEvict(value = {"deposits", "cards"}, allEntries = true)
    public void deleteDeposit(Long depositId) {
        Deposit deposit = depositRepository.findById(depositId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Deposit is not valid.")
        );
        Card card = deposit.getCardDeposit();
        if (deposit.getExpirationDate().isBefore(LocalDateTime.now())) {
            card.setBalance(card.getBalance().add(deposit.getDepositAmount()));
            card.setDepositTransaction(null);
            cardRepository.save(card);
        } else if (deposit.getExpirationDate().isAfter(LocalDateTime.now())) {
            BigDecimal returnAmount = deposit.getDepositAmount().multiply(BigDecimal.valueOf(1.05)); // 5% bonus
            card.setBalance(card.getBalance().add(returnAmount));
            card.setDepositTransaction(null);
            cardRepository.save(card);
        }
        depositRepository.delete(deposit);
    }
}
