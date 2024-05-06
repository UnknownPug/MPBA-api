package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.service.component.Generator;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.*;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CardRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.TransferRepository;
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
import java.util.Objects;

/**
 * This class is responsible for managing transfers.
 * It is annotated with @Service to indicate that it's a Spring managed service.
 * It uses TransferRepository, CardRepository, and CurrencyDataRepository to interact with the database.
 */
@Service
public class TransferService {
    private final TransferRepository transferRepository;
    private final CardRepository cardRepository;
    private final CurrencyDataService currencyDataService;

    /**
     * Constructs a new TransferService with the given repositories.
     *
     * @param transferRepository The TransferRepository to use.
     * @param cardRepository     The CardRepository to use.
     * @param currencyDataService The CurrencyDataService to use.
     */
    @Autowired
    public TransferService(TransferRepository transferRepository, CardRepository cardRepository,
                           CurrencyDataService currencyDataService) {
        this.transferRepository = transferRepository;
        this.cardRepository = cardRepository;
        this.currencyDataService = currencyDataService;
    }

    /**
     * Retrieves all transfers.
     *
     * @return A list of all transfers.
     */
    @Cacheable(value = "transfers")
    public List<Transfer> getTransfers() {
        return transferRepository.findAll();
    }

    /**
     * Retrieves transfers with filtering and sorting.
     *
     * @param pageable The pagination information.
     * @return A page of transfers.
     */
    @Cacheable(value = "transfers")
    public Page<Transfer> filterAndSortTransfers(Pageable pageable) {
        return transferRepository.findAll(pageable);
    }

    /**
     * Retrieves a transfer by its ID.
     *
     * @param transferId The ID of the transfer to retrieve.
     * @return The retrieved transfer.
     */
    @Cacheable(value = "transfers", key = "#transferId")
    public Transfer getTransferById(Long transferId) {
        return transferRepository.findById(transferId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Transfer not found.")
        );
    }

    /**
     * Retrieves a transfer by its reference number.
     *
     * @param referenceNumber The reference number of the transfer to retrieve.
     * @return The retrieved transfer.
     */
    @Cacheable(value = "transfers", key = "#referenceNumber")
    public Transfer getTransferByReferenceNumber(String referenceNumber) {
        if (referenceNumber.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Transfer is not found.");
        }
        return transferRepository.findByReferenceNumber(referenceNumber);
    }

    /**
     * Creates a transfer from one card to another.
     *
     * @param senderId           The ID of the sender's card.
     * @param receiverCardNumber The card number of the receiver.
     * @param amount             The amount to transfer.
     * @param description        The description of the transfer.
     * @return The created transfer.
     */
    @CacheEvict(value = {"transfers", "cards"}, allEntries = true)
    public Transfer createTransfer(Long senderId, String receiverCardNumber, BigDecimal amount, String description) {
        Transfer transfer = new Transfer();

        Card senderCard = cardRepository.findById(senderId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Sender card not found.")
        );
        if (senderCard.getStatus() == CardStatus.STATUS_CARD_BLOCKED) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Operation is unavailable. Sender card is blocked.");
        }
        if (senderCard.getBalance().compareTo(amount) < 0) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Insufficient funds.");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Transfer amount cannot be negative.");
        }
        Card receiverCard = cardRepository.findByCardNumber(receiverCardNumber);
        if (receiverCard == null) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Receiver card not found.");
        }
        if (receiverCard.getStatus() == CardStatus.STATUS_CARD_BLOCKED) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Operation is unavailable. Receiver card is blocked.");
        }
        if (Objects.equals(receiverCard.getStatus(), CardStatus.STATUS_CARD_BLOCKED)) {
            setDefaultTransferData(
                    "DENIED: Receiver card is not found or blocked.", transfer, senderCard, receiverCard);
            transfer.setAmount(amount);
            transfer.setStatus(FinancialStatus.DENIED);
            transferRepository.save(transfer);
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Receiver card is not found or blocked.");
        }

        setDefaultTransferData(description, transfer, senderCard, receiverCard);

        if (!senderCard.getCurrencyType().equals(receiverCard.getCurrencyType())) {
            if (senderCard.getUser().equals(receiverCard.getUser())) { // If sender and receiver have the same user
                transferCurrency(amount, senderCard.getCurrencyType(), receiverCard.getCurrencyType(), transfer);
            } else {
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "Different currency types ...");
            }
        } else { // If sender and receiver have the same currency
            transfer.setCurrency(senderCard.getCurrencyType());
            transfer.setAmount(amount);
        }

        // Updating sender balance by subtracting amount
        senderCard.setBalance(senderCard.getBalance().subtract(amount));

        // Updating receiver balance by adding amount
        receiverCard.setBalance(receiverCard.getBalance().add(transfer.getAmount()));
        cardRepository.save(senderCard);
        cardRepository.save(receiverCard);
        transfer.setStatus(FinancialStatus.RECEIVED);
        return transferRepository.save(transfer);
    }

    /**
     * Transfers currency from one card to another.
     *
     * @param amount     The amount to transfer.
     * @param senderCurrency The sender's currency.
     * @param receiverCurrency The receiver's currency.
     * @param transfer   The transfer to perform.
     */
    private void transferCurrency(BigDecimal amount, Currency senderCurrency, Currency receiverCurrency, Transfer transfer) {
        switch (senderCurrency) {
            case USD:
                transfer.setAmount(amount.multiply(
                        BigDecimal.valueOf(currencyDataService.convertCurrency(
                                senderCurrency.toString(), receiverCurrency.toString()).getRate()))
                );
                transfer.setCurrency(Currency.USD);
                break;
            case EUR:
                transfer.setAmount(amount.multiply(
                        BigDecimal.valueOf(currencyDataService.convertCurrency(
                                senderCurrency.toString(), receiverCurrency.toString()).getRate())
                ));
                transfer.setCurrency(Currency.EUR);
                break;
            case UAH:
                transfer.setAmount(amount.multiply(
                        BigDecimal.valueOf(currencyDataService.convertCurrency(
                                senderCurrency.toString(), receiverCurrency.toString()).getRate())
                ));
                transfer.setCurrency(Currency.UAH);
                break;
            case CZK:
                transfer.setAmount(amount.multiply(
                        BigDecimal.valueOf(currencyDataService.convertCurrency(
                                        senderCurrency.toString(), receiverCurrency.toString()).getRate())
                ));
                transfer.setCurrency(Currency.CZK);
                break;
            case PLN:
                transfer.setAmount(amount.multiply(
                        BigDecimal.valueOf(currencyDataService.convertCurrency(
                                senderCurrency.toString(), receiverCurrency.toString()).getRate())
                ));
                transfer.setCurrency(Currency.PLN);
                break;
        }
    }

    /**
     * Sets the default data for a transfer.
     *
     * @param description  The description of the transfer.
     * @param transfer     The transfer to set the data for.
     * @param senderCard   The sender's card.
     * @param receiverCard The receiver's card.
     */
    private void setDefaultTransferData(String description, Transfer transfer, Card senderCard, Card receiverCard) {
        Generator generator = new Generator();
        transfer.setSenderCard(senderCard);
        transfer.setReceiverCard(receiverCard);
        transfer.setDateTime(LocalDateTime.now());
        String referenceNumber;
        do {
            referenceNumber = HtmlUtils.htmlEscape(generator.generateReferenceNumber());
            if (!isValidReferenceNumber(referenceNumber)) {
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid reference number.");
            }
        } while (transferRepository.existsByReferenceNumber(referenceNumber));
        transfer.setReferenceNumber(referenceNumber);
        if (!isValidDescription(description)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Description must be between 1 and 100 characters.");
        }
        transfer.setDescription(HtmlUtils.htmlEscape(description));
    }

    /**
     * Checks if a reference number is valid.
     *
     * @param referenceNumber The reference number to check.
     * @return True if the reference number is valid, false otherwise.
     */
    private boolean isValidReferenceNumber(String referenceNumber) {
        return referenceNumber != null && !referenceNumber.isEmpty() && referenceNumber.length() <= 11;
    }

    /**
     * Checks if a description is valid.
     *
     * @param description The description to check.
     * @return True if the description is valid, false otherwise.
     */
    private boolean isValidDescription(String description) {
        return description != null && !description.isEmpty() && description.length() <= 100;
    }
}
