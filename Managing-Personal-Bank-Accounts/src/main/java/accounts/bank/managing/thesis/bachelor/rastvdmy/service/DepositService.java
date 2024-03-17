package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Deposit;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.DepositRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public Deposit createDeposit(String description) {
        // TODO: complete this method
        return null;
    }

    public void updateDeposit(Long depositId, String description) {
        // TODO: complete this method
        // The deposit cannot be renewed before the end of the deposit period
        // (with the condition of not improving/deteriorating).
    }

    public void deleteDeposit(Long depositId) {
        // TODO: check this method
        depositRepository.findById(depositId).orElseThrow(
                () -> new IllegalArgumentException("Deposit is not valid")
        );
        depositRepository.deleteById(depositId);
    }
}
