package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Deposit;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.DepositRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class DepositService {
    private final DepositRepository depositRepository;

    @Autowired
    public DepositService(DepositRepository depositRepository) {
        this.depositRepository = depositRepository;
    }

    public List<Deposit> getAllDeposits() {
        return depositRepository.findAll();
    }

    public Deposit getAllDepositById(Long id) {
        return depositRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Deposit is not valid")
        );
    }

    public Deposit createDeposit(String description, Integer referenceNumber) {
        Deposit deposit = new Deposit();
        deposit.setDescription(description);
        deposit.setReferenceNumber(referenceNumber);
        deposit.setStartDate(LocalDateTime.now());
        deposit.setExpirationDate(LocalDateTime.now().plusYears(1));
        return depositRepository.save(deposit);
    }


    public Object updateDeposit(Long depositId, String description, Integer referenceNumber) {
        Deposit deposit = depositRepository.findById(depositId).orElseThrow(
                () -> new IllegalArgumentException("Deposit is not valid")
        );
        if (description.isEmpty() && Objects.equals(deposit.getDescription(), description)) {
            throw new IllegalArgumentException("Description is not valid");
        }
        if (referenceNumber.equals(deposit.getReferenceNumber())) {
            throw new IllegalArgumentException("Reference number is not valid");
        }
        deposit.setDescription(description);
        deposit.setReferenceNumber(referenceNumber);
        return depositRepository.save(deposit);
    }

    public void updateDepositDescription(Long depositId, String description) {
        Deposit deposit = depositRepository.findById(depositId).orElseThrow(
                () -> new IllegalArgumentException("Deposit is not valid")
        );
        if (description.isEmpty() && Objects.equals(deposit.getDescription(), description)) {
            throw new IllegalArgumentException("Description is not valid");
        }
        deposit.setDescription(description);
    }

    public void updateDepositReferenceNumber(Long depositId, Integer referenceNumber) {
        Deposit deposit = depositRepository.findById(depositId).orElseThrow(
                () -> new IllegalArgumentException("Deposit is not valid")
        );
        if (referenceNumber.equals(deposit.getReferenceNumber())) {
            throw new IllegalArgumentException("Reference number is not valid");
        }
        deposit.setReferenceNumber(referenceNumber);
    }

    public void deleteDeposit(Long depositId) {
        depositRepository.findById(depositId).orElseThrow(
                () -> new IllegalArgumentException("Deposit is not valid")
        );
        depositRepository.deleteById(depositId);
    }
}
