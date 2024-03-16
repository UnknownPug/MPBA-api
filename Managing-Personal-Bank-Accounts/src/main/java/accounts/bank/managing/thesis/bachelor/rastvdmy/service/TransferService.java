package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Transfer;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.TransferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class TransferService {

    private final TransferRepository transferRepository;

    @Autowired
    public TransferService(TransferRepository transferRepository) {
        this.transferRepository = transferRepository;
    }

    public List<Transfer> getTransfers() {
        return transferRepository.findAll();
    }

    public Transfer getTransferById(Long transferId) {
        return transferRepository.findById(transferId).orElseThrow(
                () -> new RuntimeException("Transfer not found")
        );
    }

    public Transfer createTransfer(BigDecimal amount, String description, BigDecimal commission) {
        Transfer transfer = new Transfer();
        transfer.setAmount(amount);
        transfer.setDescription(description);
        transfer.setCommission(commission);
        transfer.setDateTime(LocalDateTime.now());
        return transferRepository.save(transfer);
    }

    public void updateTransfer(Long transferId, BigDecimal amount, String description, BigDecimal commission) {
        Transfer transfer = transferRepository.findById(transferId).orElseThrow(
                () -> new RuntimeException("Transfer not found")
        );
        if (description == null || Objects.equals(description, transfer.getDescription())) {
            throw new IllegalArgumentException("Description is not valid");
        }
        if (amount == null || Objects.equals(amount, transfer.getAmount()) || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount is not valid");
        }
        if (commission == null || Objects.equals(commission, transfer.getCommission()) || commission.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Commission is not valid");
        }
        transfer.setAmount(amount);
        transfer.setDescription(description);
        transfer.setCommission(commission);
        transferRepository.save(transfer);
    }
}
