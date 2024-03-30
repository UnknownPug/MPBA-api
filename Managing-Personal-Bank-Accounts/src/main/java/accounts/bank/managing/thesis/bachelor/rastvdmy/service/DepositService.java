package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.service.component.Generator;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Card;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Currency;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Deposit;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CardRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CurrencyDataRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.DepositRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<Deposit> getAllDeposits() {
        return depositRepository.findAll();
    }

    public Deposit getAllDepositById(Long id) {
        return depositRepository.findById(id).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Deposit is not found")
        );
    }

    @Transactional
    public Deposit openDeposit(String cardNumber, BigDecimal depositAmount, String description, Currency currency) {
        Deposit deposit = new Deposit();
        Card card = cardRepository.findByCardNumber(cardNumber);
        if (card == null) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Card is not found");
        }
        deposit.setCurrency(currency);
        if (card.getBalance().compareTo(depositAmount) < 0) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Not enough money on the card");
        }

        deposit.setDepositCard(generator.generateAccountNumber());
        deposit.setDepositAmount(convertCurrency(deposit, depositAmount));
        deposit.setCardDeposit(card);
        deposit.setDescription(description);
        deposit.setStartDate(LocalDateTime.now());
        deposit.setExpirationDate(deposit.getStartDate().plusYears(1));
        deposit.setReferenceNumber(generator.generateReferenceNumber());
        depositRepository.save(deposit);

        card.setDepositTransaction(deposit);
        card.setBalance(card.getBalance().subtract(depositAmount));
        cardRepository.save(card);

        return deposit;
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
    @Transactional
    public void updateDeposit(Long depositId, String cardNumber, String description, BigDecimal bigDecimal, Currency currency) {
        Deposit deposit = depositRepository.findById(depositId).orElseThrow(
                () -> new IllegalArgumentException("Deposit is not valid")
        );
        Card card = cardRepository.findByCardNumber(cardNumber);
        if (card == null) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Card is not found");
        }
        if (deposit.getCardDeposit().equals(card)) {
            if (deposit.getExpirationDate().isBefore(LocalDateTime.now())) {
                if (card.getCurrencyType().equals(deposit.getCurrency())) {
                    card.setBalance(card.getBalance().add(deposit.getDepositAmount()));
                } else {
                    card.setBalance(card.getBalance().add(convertCurrency(deposit, deposit.getDepositAmount())));
                }
                cardRepository.save(card);
                deleteDeposit(depositId);
            } else if (deposit.getExpirationDate().isAfter(LocalDateTime.now()) || deposit.getExpirationDate().isEqual(LocalDateTime.now())) {
                openDeposit(cardNumber, bigDecimal, description, currency);
            }
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Card must be the same as the card that was used for the deposit");
        }
    }

    public void deleteDeposit(Long depositId) {
        depositRepository.findById(depositId).orElseThrow(
                () -> new IllegalArgumentException("Deposit is not valid")
        );
        depositRepository.deleteById(depositId);
    }
}
