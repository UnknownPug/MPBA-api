package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.CurrencyData;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CurrencyDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrencyDataService {
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

    public CurrencyData addCurrencyData(String currencyCode) {
        CurrencyData currencyData = new CurrencyData();
        if (currencyCode.isEmpty()) {
            throw new IllegalStateException("Currency code not valid.");
        }
        currencyData.setCurrencyCode(currencyCode);
        return currencyDataRepository.save(currencyData);
    }

    public void updateCurrencyData(Long id, String currencyCode) {
        CurrencyData currencyData = currencyDataRepository.findById(id).orElseThrow(
                () -> new IllegalStateException("Currency data with id " + id + " does not exist.")
        );
        if (currencyCode.isEmpty()) {
            throw new IllegalStateException("Currency code not valid.");
        }
        currencyData.setCurrencyCode(currencyCode);
        currencyDataRepository.save(currencyData);
    }

    public void deleteCurrencyData(Long id) {
        currencyDataRepository.findById(id).orElseThrow(
                () -> new IllegalStateException("Currency data with id " + id + " does not exist.")
        );
        currencyDataRepository.deleteById(id);
    }
}
