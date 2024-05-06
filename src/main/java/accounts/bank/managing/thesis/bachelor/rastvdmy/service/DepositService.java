package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.CardStatus;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CurrencyDataRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.component.Generator;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Card;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Currency;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Deposit;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CardRepository;
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

/**
 * This class is responsible for managing deposits.
 * It is annotated with @Service to indicate that it's a Spring managed service.
 * It uses DepositRepository, Generator, CurrencyDataRepository, and CardRepository to interact with the database.
 */
@Service
public class DepositService {
    private final DepositRepository depositRepository;
    private final Generator generator;
    private final CurrencyDataService currencyDataService;
    private final CardRepository cardRepository;
    private final CurrencyDataRepository currencyRepository;

    /**
     * Constructs a new DepositService with the given repositories and generator.
     *
     * @param depositRepository   The DepositRepository to use.
     * @param generator           The Generator to use.
     * @param currencyDataService The CurrencyDataService to use.
     * @param cardRepository      The CardRepository to use.
     */
    @Autowired
    public DepositService(DepositRepository depositRepository, Generator generator,
                          CurrencyDataService currencyDataService, CardRepository cardRepository,
                          CurrencyDataRepository currencyRepository) {
        this.depositRepository = depositRepository;
        this.generator = generator;
        this.currencyDataService = currencyDataService;
        this.cardRepository = cardRepository;
        this.currencyRepository = currencyRepository;
    }

    /**
     * Retrieves all deposits.
     *
     * @return A list of all deposits.
     */
    @Cacheable(value = "deposits")
    public List<Deposit> getAllDeposits() {
        return depositRepository.findAll();
    }

    /**
     * Retrieves deposits with filtering and sorting.
     *
     * @param pageable The pagination information.
     * @return A page of deposits.
     */
    @Cacheable(value = "deposits")
    public Page<Deposit> filterAndSortDeposits(Pageable pageable) {
        return depositRepository.findAll(pageable);
    }

    /**
     * Retrieves a deposit by its ID.
     *
     * @param id The ID of the deposit to retrieve.
     * @return The retrieved deposit.
     */
    @Cacheable(value = "deposits", key = "#id")
    public Deposit getDepositById(Long id) {
        return depositRepository.findById(id).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Deposit is not found.")
        );
    }

    /**
     * Opens a deposit for a specific card.
     *
     * @param cardNumber    The card number for the deposit.
     * @param depositAmount The amount of the deposit.
     * @param description   The description of the deposit.
     * @param currency      The currency of the deposit.
     * @return The created deposit.
     */
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

    /**
     * Checks if the provided deposit card number is valid.
     *
     * @param depositCard The deposit card number to check.
     * @return True if the deposit card number is not null
     * and matches the pattern of 10 digits followed by a forward slash and 4 digits, false otherwise.
     */
    private boolean isValidDepositCard(String depositCard) {
        // Regular expression to match 10 digits followed by a forward slash and 4 digits
        String regex = "^\\d{10}/\\d{4}$";
        return depositCard != null && depositCard.matches(regex);
    }

    /**
     * Checks if the provided reference number is valid.
     *
     * @param referenceNumber The reference number to check.
     * @return True if the reference number is not null, not empty, and its length is less than or equal to 11, false otherwise.
     */
    private boolean isValidReferenceNumber(String referenceNumber) {
        return referenceNumber != null && !referenceNumber.isEmpty() && referenceNumber.length() <= 11;
    }

    /**
     * Checks if the provided description is valid.
     *
     * @param description The description to check.
     * @return True if the description is not null, not empty,
     * and its length is less than or equal to 100 characters, false otherwise.
     */
    private boolean isValidDescription(String description) {
        return description != null && !description.isEmpty() && description.length() <= 100;
    }

    /**
     * Converts the deposit amount to the currency of the deposit.
     *
     * @param deposit       The deposit to convert the amount for.
     * @param depositAmount The amount to convert.
     * @return The converted amount.
     */
    private BigDecimal convertCurrency(Deposit deposit, BigDecimal depositAmount) {
        Currency depositCurrency = deposit.getCurrency();
        return convertCurrencyToDeposit(depositCurrency, depositAmount);
    }

    /**
     * Updates a deposit.
     *
     * @param depositId   The ID of the deposit to update.
     * @param cardNumber  The card number for the deposit.
     * @param description The description of the deposit.
     * @param newAmount   The new amount of the deposit.
     * @param currency    The currency of the deposit.
     */
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

    /**
     * Retrieves a card by its card number and checks if the card has enough balances for a new amount.
     *
     * @param cardNumber The card number to retrieve the card.
     * @param newAmount  The new amount to check if the card has enough balances.
     * @return The retrieved card, if it exists, is not blocked, and has enough balances for the new amount.
     * @throws ApplicationException if the card is not found, is blocked, or does not have enough balances for the new amount.
     */
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

    /**
     * Deletes a deposit.
     *
     * @param depositId The ID of the deposit to delete.
     */
    @CacheEvict(value = {"deposits", "cards"}, allEntries = true)
    public void deleteDeposit(Long depositId) {
        Deposit deposit = depositRepository.findById(depositId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Deposit is not valid.")
        );
        Card card = deposit.getCardDeposit();
        if (LocalDateTime.now().isBefore(deposit.getExpirationDate())) {
            BigDecimal returnAmount;
            if (card.getCurrencyType().equals(deposit.getCurrency())) {
                returnAmount = deposit.getDepositAmount();
            } else {
                returnAmount = convertCurrencyCase(
                        card.getCurrencyType(), deposit.getCurrency(), deposit.getDepositAmount());
            }
            card.setBalance(card.getBalance().add(returnAmount));
            card.setDepositTransaction(null);
            cardRepository.save(card);
        } else if (LocalDateTime.now().isAfter(deposit.getExpirationDate())) {
            BigDecimal returnAmount = deposit.getDepositAmount().multiply(BigDecimal.valueOf(1.05)); // 5% bonus
            if (card.getCurrencyType().equals(deposit.getCurrency())) {
                returnAmount = deposit.getDepositAmount();
            } else {
                returnAmount = convertCurrencyCase(card.getCurrencyType(), deposit.getCurrency(), returnAmount);
            }
            card.setBalance(card.getBalance().add(returnAmount));
            card.setDepositTransaction(null);
            cardRepository.save(card);
        }
        depositRepository.delete(deposit);
    }

    /**
     * Converts the deposit amount to the currency of the deposit.
     *
     * @param currency The currency to convert the amount for.
     * @param amount   The amount to convert.
     * @return The converted amount.
     */
    private BigDecimal convertCurrencyToDeposit(Currency currency, BigDecimal amount) {
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

    /**
     * Converts the deposit amount to the currency of the card.
     *
     * @param cardCurrency    The currency of the card.
     * @param depositCurrency The currency of the deposit.
     * @param amount          The amount to convert.
     * @return The converted amount.
     */
    private BigDecimal convertCurrencyCase(Currency cardCurrency, Currency depositCurrency, BigDecimal amount) {
        return switch (cardCurrency) {
            case USD, UAH, EUR, CZK, PLN -> amount.multiply(
                    BigDecimal.valueOf(currencyDataService.convertCurrency(
                            depositCurrency.toString(), cardCurrency.toString()).getRate())
            );
        };
    }
}
