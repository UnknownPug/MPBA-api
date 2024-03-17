package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.CurrencyData;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CurrencyDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrencyDataService {
    // TODO: complete this class by adding Currency API
    private final CurrencyDataRepository currencyDataRepository;

    @Autowired
    public CurrencyDataService(CurrencyDataRepository currencyDataRepository) {
        this.currencyDataRepository = currencyDataRepository;
    }

    public List<CurrencyData> getAllCurrencyData() {
        return currencyDataRepository.findAll();
    }

    public CurrencyData getCurrencyDataById(Long id) {
        return currencyDataRepository.findById(id).orElseThrow(
                () -> new IllegalStateException("Currency data with id " + id + " does not exist.")
        );
    }
}
