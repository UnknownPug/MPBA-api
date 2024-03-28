package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.*;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CardRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CurrencyDataRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.TransferRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<Transfer> getTransfers() {
        return transferRepository.findAll();
    }

    public Transfer getTransferById(Long transferId) {
        return transferRepository.findById(transferId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Transfer not found")
        );
    }

    public Transfer getTransferByReferenceNumber(String referenceNumber) {
        if (referenceNumber.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Transfer is not found.");
        }
        return transferRepository.findByReferenceNumber(referenceNumber);
    }

    public Transfer createTransfer(Long senderId, String receiverCardNumber, BigDecimal amount, String description) {
        Transfer transfer = new Transfer();

        Card senderCard = cardRepository.findById(senderId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Sender card not found")
        );

        if (senderCard.getBalance().compareTo(amount) < 0) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }
        senderCard.setSenderTransferTransaction(transfer);

        Card receiverCard = cardRepository.findByCardNumber(receiverCardNumber);
        if (receiverCard == null) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Receiver card not found");
        }
        receiverCard.setReceiverTransferTransaction(transfer);
        if (Objects.equals(receiverCard.getStatus(), CardStatus.STATUS_CARD_BLOCKED)) {
            setDefaultTransferData(
                    "DENIED: Receiver card is not found or blocked.", transfer, senderCard, receiverCard);
            transfer.setAmount(amount);
            transfer.setStatus(FinancialStatus.DENIED);
            transferRepository.save(transfer);
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Receiver card is not found or blocked");
        }

        setDefaultTransferData(description, transfer, senderCard, receiverCard);

        if (!senderCard.getCurrencyType().equals(receiverCard.getCurrencyType())) {
            if (senderCard.getUser().equals(receiverCard.getUser())) { // If sender and receiver have the same user
                transferCurrency(amount, senderCard, transfer);
            } else {
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "Different currency types ...");
            }
        } else { // If sender and receiver have the same currency
            transfer.setAmount(amount);
        }

        // Updating sender balance by subtracting amount
        senderCard.setBalance(senderCard.getBalance().subtract(amount));

        // Updating receiver balance by adding amount
        receiverCard.setBalance(receiverCard.getBalance().add(transfer.getAmount()));

        transfer.setStatus(FinancialStatus.RECEIVED);
        cardRepository.save(senderCard);
        cardRepository.save(receiverCard);
        return transferRepository.save(transfer);
    }

    private void transferCurrency(BigDecimal amount, Card senderCard, Transfer transfer) {
        switch (senderCard.getCurrencyType()) {
            case USD:
                transfer.setAmount(amount.multiply(
                        BigDecimal.valueOf(
                                currencyRepository.findByCurrency(Currency.USD.toString()).getRate())
                ));
                break;
            case EUR:
                transfer.setAmount(amount.multiply(
                        BigDecimal.valueOf(currencyRepository.findByCurrency(Currency.EUR.toString()).getRate())
                ));
                break;
            case UAH:
                transfer.setAmount(amount.multiply(
                        BigDecimal.valueOf(currencyRepository.findByCurrency(Currency.UAH.toString()).getRate())
                ));
                break;
            case CZK:
                transfer.setAmount(amount.multiply(
                        BigDecimal.valueOf(currencyRepository.findByCurrency(Currency.CZK.toString()).getRate())
                ));
                break;
            case PLN:
                transfer.setAmount(amount.multiply(
                        BigDecimal.valueOf(currencyRepository.findByCurrency(Currency.PLN.toString()).getRate())
                ));
                break;
            default:
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "Currency type is not supported ...");
        }
    }

    private static void setDefaultTransferData(String description, Transfer transfer,
                                               Card senderCard, Card receiverCard) {
        Generator generator = new Generator();
        transfer.setSenderCard(senderCard);
        transfer.setReceiverCard(receiverCard);
        transfer.setDateTime(LocalDateTime.now());
        transfer.setReferenceNumber(generator.generateReferenceNumber());
        transfer.setDescription(description);
    }
}
