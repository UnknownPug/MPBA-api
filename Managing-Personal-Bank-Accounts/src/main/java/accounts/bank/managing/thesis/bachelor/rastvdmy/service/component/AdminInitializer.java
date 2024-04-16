package accounts.bank.managing.thesis.bachelor.rastvdmy.service.component;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.*;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CurrencyDataRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;

@Component
public class AdminInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(AdminInitializer.class);

    private UserRepository repository;
    private PasswordEncoder passwordEncoder;
    private CurrencyDataRepository currencyDataRepository;

    @Autowired
    public AdminInitializer(UserRepository repository, PasswordEncoder passwordEncoder, CurrencyDataRepository currencyDataRepository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.currencyDataRepository = currencyDataRepository;
    }

    public AdminInitializer() {
    }

    public void initializeAdmin() {
        if (!repository.existsByUserRole(UserRole.ROLE_ADMIN)) {
            LOG.info("Initializing admin...");
            User admin = new User();
            admin.setId(1L);
            admin.setUserRole(UserRole.ROLE_ADMIN);
            admin.setVisibility(UserVisibility.STATUS_ONLINE);
            admin.setName("Admin");
            admin.setSurname("DR");

            admin.setDateOfBirth(LocalDate.of(2001, Calendar.SEPTEMBER, 1));
            admin.setCountryOrigin("Czechia");
            admin.setEmail("adminbank@czechbank.com");
            admin.setPassword("Admin123");
            admin.encodePassword(passwordEncoder);
            admin.setAvatar("https://www.shareicon.net/data/2015/09/18/103157_man_512x512.png");
            admin.setPhoneNumber("+420123456789");

            List<CurrencyData> currencyData = currencyDataRepository.findAll();
            admin.setCurrencyData(currencyData);
            admin.setBankLoan(null);
            admin.setCards(null);
            repository.save(admin);
        }
        LOG.info("Admin has been already initialized");
    }
}
