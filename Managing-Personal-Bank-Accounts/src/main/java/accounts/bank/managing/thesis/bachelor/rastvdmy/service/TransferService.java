package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.service.component.Generator;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.*;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CardRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CurrencyDataRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.TransferRepository;
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
import java.util.Objects;

@Service
public class TransferService {
    private final TransferRepository transferRepository;
    private final CardRepository cardRepository;
    private final CurrencyDataRepository currencyRepository;

    @Autowired
    public TransferService(TransferRepository transferRepository, CardRepository cardRepository, CurrencyDataRepository currencyRepository) {
        this.transferRepository = transferRepository;
        this.cardRepository = cardRepository;
        this.currencyRepository = currencyRepository;
    }

    @Cacheable(value = "transfers")
    public List<Transfer> getTransfers() {
        return transferRepository.findAll();
    }

    @Cacheable(value = "transfers")
    public Page<Transfer> filterAndSortTransfers(Pageable pageable) {
        return transferRepository.findAll(pageable);
    }

    @Cacheable(value = "transfers", key = "#transferId")
    public Transfer getTransferById(Long transferId) {
        return transferRepository.findById(transferId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Transfer not found.")
        );
    }

    @Cacheable(value = "transfers", key = "#referenceNumber")
    public Transfer getTransferByReferenceNumber(String referenceNumber) {
        if (referenceNumber.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Transfer is not found.");
        }
        return transferRepository.findByReferenceNumber(referenceNumber);
    }

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

        Card receiverCard = cardRepository.findByCardNumber(receiverCardNumber);
        if (receiverCard == null) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Receiver card not found.");
        }
        if (receiverCard.getStatus() == CardStatus.STATUS_CARD_BLOCKED) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Operation is unavailable. Receiver card is blocked.");
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
                transferCurrency(amount, senderCard, transfer);
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

    private void transferCurrency(BigDecimal amount, Card senderCard, Transfer transfer) {
        switch (senderCard.getCurrencyType()) {
            case USD:
                transfer.setAmount(amount.multiply(
                        BigDecimal.valueOf(
                                currencyRepository.findByCurrency(Currency.USD.toString()).getRate())
                ));
                transfer.setCurrency(Currency.USD);
                break;
            case EUR:
                transfer.setAmount(amount.multiply(
                        BigDecimal.valueOf(currencyRepository.findByCurrency(Currency.EUR.toString()).getRate())
                ));
                transfer.setCurrency(Currency.EUR);
                break;
            case UAH:
                transfer.setAmount(amount.multiply(
                        BigDecimal.valueOf(currencyRepository.findByCurrency(Currency.UAH.toString()).getRate())
                ));
                transfer.setCurrency(Currency.UAH);
                break;
            case CZK:
                transfer.setAmount(amount.multiply(
                        BigDecimal.valueOf(currencyRepository.findByCurrency(Currency.CZK.toString()).getRate())
                ));
                transfer.setCurrency(Currency.CZK);
                break;
            case PLN:
                transfer.setAmount(amount.multiply(
                        BigDecimal.valueOf(currencyRepository.findByCurrency(Currency.PLN.toString()).getRate())
                ));
                transfer.setCurrency(Currency.PLN);
                break;
        }
    }

    private void setDefaultTransferData(String description, Transfer transfer, Card senderCard, Card receiverCard) {
        Generator generator = new Generator();
        transfer.setSenderCard(senderCard);
        transfer.setReceiverCard(receiverCard);
        transfer.setDateTime(LocalDateTime.now());
        String referenceNumber;
        do {
            referenceNumber = generator.generateReferenceNumber();
        } while (transferRepository.existsByReferenceNumber(referenceNumber));
        transfer.setReferenceNumber(referenceNumber);
        transfer.setDescription(description);
    }
}
