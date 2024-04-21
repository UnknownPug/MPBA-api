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
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Currency.*;

@Service
public class DepositService {
    private final DepositRepository depositRepository;
    private final Generator generator;
    private final CurrencyDataRepository currencyRepository;
    private final CardRepository cardRepository;

    @Autowired
    public DepositService(DepositRepository depositRepository, Generator generator,
                          CurrencyDataRepository currencyRepository, CardRepository cardRepository) {
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
        String depositCard = HtmlUtils.htmlEscape(generator.generateAccountNumber());
        if (!isValidDepositCard(depositCard)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid deposit card.");
        }
        deposit.setDepositCard(depositCard);
        if (depositAmount.compareTo(BigDecimal.valueOf(0)) <= 0) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Deposit amount must be greater than 0.");
        }
        deposit.setDepositAmount(convertCurrency(deposit, depositAmount));
        deposit.setCardDeposit(card);
        if (!isValidDescription(description)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "The length of the description must be between 1 and 100 characters.");
        }
        deposit.setDescription(HtmlUtils.htmlEscape(description));
        deposit.setStartDate(LocalDateTime.now());
        deposit.setExpirationDate(deposit.getStartDate().plusYears(1));
        String referenceNumber;
        do {
            referenceNumber = HtmlUtils.htmlEscape(generator.generateReferenceNumber());
            if (!isValidReferenceNumber(referenceNumber)) {
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid reference number.");
            }
        } while (depositRepository.existsByReferenceNumber(referenceNumber));
        deposit.setReferenceNumber(referenceNumber);
        card.setBalance(card.getBalance().subtract(
                depositAmount.multiply(BigDecimal.valueOf(
                        currencyRepository.findByCurrency(card.getCurrencyType().toString()).getRate())
                )));
        cardRepository.save(card);

        return depositRepository.save(deposit);
    }

    private boolean isValidDepositCard(String depositCard) {
        // Regular expression to match 10 digits followed by a forward slash and 4 digits
        String regex = "^\\d{10}/\\d{4}$";
        return depositCard != null && depositCard.matches(regex);
    }

    private boolean isValidReferenceNumber(String referenceNumber) {
        return referenceNumber != null && !referenceNumber.isEmpty() && referenceNumber.length() <= 11;
    }

    private boolean isValidDescription(String description) {
        return description != null && !description.isEmpty() && description.length() <= 100;
    }

    private BigDecimal convertCurrency(Deposit deposit, BigDecimal depositAmount) {
        Currency depositCurrency = deposit.getCurrency();
        return convertCurrencyCase(depositCurrency, depositAmount);
    }

    // The deposit cannot be renewed before the end of the deposit period
    // (with the condition of not improving/deteriorating).
    @CacheEvict(value = {"deposits", "cards"}, allEntries = true)
    public void updateDeposit(Long depositId, String cardNumber, String description,
                              BigDecimal newAmount, Currency currency) {
        Deposit deposit = depositRepository.findById(depositId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Deposit is not valid.")
        );
        Card card = getUserCard(cardNumber, newAmount);
        if (deposit.getCardDeposit().equals(card)) {
            deleteDeposit(depositId); // Delete the deposit after the money has been returned to the card
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Card must be the same as the card that was used for the deposit.");
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
            BigDecimal returnAmount;
            if (card.getCurrencyType().equals(deposit.getCurrency())) {
                returnAmount = deposit.getDepositAmount();
            } else {
                returnAmount = convertToCardCurrency(card, deposit.getDepositAmount());
            }
            card.setBalance(card.getBalance().add(returnAmount));
            card.setDepositTransaction(null);
            cardRepository.save(card);
        } else if (deposit.getExpirationDate().isAfter(LocalDateTime.now())) {
            BigDecimal returnAmount = deposit.getDepositAmount().multiply(BigDecimal.valueOf(1.05)); // 5% bonus
            if (card.getCurrencyType().equals(deposit.getCurrency())) {
                returnAmount = deposit.getDepositAmount();
            } else {
                returnAmount = convertToCardCurrency(card, returnAmount);
            }
            card.setBalance(card.getBalance().add(returnAmount));
            card.setDepositTransaction(null);
            cardRepository.save(card);
        }
        depositRepository.delete(deposit);
    }

    private BigDecimal convertToCardCurrency(Card card, BigDecimal amount) {
        Currency cardCurrency = card.getCurrencyType();
        return convertCurrencyCase(cardCurrency, amount);
    }

    private BigDecimal convertCurrencyCase(Currency currency, BigDecimal amount) {
        return switch (currency) {
            case USD -> amount.multiply(
                    BigDecimal.valueOf(currencyRepository.findByCurrency(USD.toString()).getRate())
            );
            case EUR -> amount.multiply(
                    BigDecimal.valueOf(currencyRepository.findByCurrency(EUR.toString()).getRate())
            );
            case CZK -> amount.multiply(
                    BigDecimal.valueOf(currencyRepository.findByCurrency(CZK.toString()).getRate())
            );
            case UAH -> amount.multiply(
                    BigDecimal.valueOf(currencyRepository.findByCurrency(UAH.toString()).getRate())
            );
            case PLN -> amount.multiply(
                    BigDecimal.valueOf(currencyRepository.findByCurrency(PLN.toString()).getRate())
            );
        };
    }
}
