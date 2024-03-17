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

    public Transfer createTransfer(Long senderId, Long receiverId, String receiverCardNumber,
                                   BigDecimal amount, String description) {
        // TODO: complete this method
        return null;
    }
}
