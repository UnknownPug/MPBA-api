package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.*;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CurrencyDataRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrencyDataRepository currencyDataRepository;
    private final CardService cardService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CurrencyDataRepository currencyDataRepository, CardService cardService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.currencyDataRepository = currencyDataRepository;
        this.cardService = cardService;
    }

    @Cacheable(value = "users")
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Cacheable(value = "users")
    public Page<User> filterAndSortUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Cacheable(value = "users", key = "#userId")
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found.")
        );
    }

    @CachePut(value = "users", key = "#result.id")
    public User createUser(String name, String surname, LocalDate dateOfBirth, String countryOfOrigin,
                           String email, String password, String phoneNumber) {
        if (name.isEmpty() || surname.isEmpty() || countryOfOrigin.isEmpty() ||
                email.isEmpty() || password.isEmpty() || phoneNumber.isEmpty()) {
            throw new ApplicationException(HttpStatus.NO_CONTENT, "All user fields must be filled.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Email " + email + " is unavailable.");
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Phone number " + phoneNumber + " is unavailable.");
        }
        // Creating user with default role and status
        User user = new User();
        user.setName(name);
        user.setSurname(surname);
        user.setDateOfBirth(dateOfBirth);
        if (countryExists(countryOfOrigin)) {
            user.setCountryOrigin(countryOfOrigin);
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Country " + countryOfOrigin + " does not exist.");
        }
        user.setEmail(email);
        user.setPassword(password);
        user.encodePassword(passwordEncoder);
        user.setPhoneNumber(phoneNumber);
        user.setUserRole(UserRole.ROLE_USER);
        user.setStatus(UserStatus.STATUS_ONLINE);

        List<CurrencyData> currencyData = currencyDataRepository.findAll();
        user.setCurrencyData(currencyData);
        // Save the user and get the saved instance. Then create a card for the user
        User savedUser = userRepository.save(user);
        Card card = cardService.createCard(savedUser.getId(), Currency.CZK.toString(), CardType.VISA.toString());
        savedUser.getCards().add(card);
        userRepository.save(savedUser);
        return savedUser;
    }

    private boolean countryExists(String countryName) {
        final String url = "https://restcountries.eu/rest/v2/name/" + countryName;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful() && response.getBody() != null && !response.getBody().isEmpty();
        } catch (RestClientException e) {
            return false;
        }
    }

    @CachePut(value = "users", key = "#userId")
    public void updateUserById(Long userId, String email, String password, String phoneNumber) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found.")
        );
        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }
        if (email == null || password == null || phoneNumber == null) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "All user fields must be filled.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Email " + email + " is unavailable.");
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Phone number " + phoneNumber + " is unavailable.");
        }
        if (Objects.equals(password, user.getPassword())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Entered password is the same as the old one.");
        }

        user.setEmail(email);
        user.setPassword(password);
        user.encodePassword(passwordEncoder);
        user.setPhoneNumber(phoneNumber);
        userRepository.save(user);
    }

    @CachePut(value = "users", key = "#userId")
    public void uploadUserAvatar(Long userId, MultipartFile userAvatar) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found.")
        );
        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }
        user.setAvatar(userAvatar.getOriginalFilename());
        userRepository.save(user);
    }

    @CachePut(value = "users", key = "#userId")
    public void updateUserEmailById(Long userId, String email) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found.")
        );
        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }
        if (email == null) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Email must be filled.");
        }
        if (Objects.equals(email, user.getEmail())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Entered email is the same as the old one.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Email " + email + " is unavailable.");
        }
        user.setEmail(email);
        userRepository.save(user);
    }

    @CachePut(value = "users", key = "#userId")
    public void updateUserPasswordById(Long userId, String password) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found.")
        );
        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }
        if (password.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Password must be filled.");
        }
        if (Objects.equals(password, user.getPassword())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Entered password is the same as the old one.");
        }
        user.setPassword(password);
        user.encodePassword(passwordEncoder);
        userRepository.save(user);
    }

    @CachePut(value = "users", key = "#userId")
    public void updateUserRoleById(Long userId, String role) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found.")
        );
        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }
        switch (role.toLowerCase()) {
            case "admin" -> user.setUserRole(UserRole.ROLE_ADMIN);
            case "moderator" -> user.setUserRole(UserRole.ROLE_MODERATOR);
            default -> user.setUserRole(UserRole.ROLE_USER);
        }
        userRepository.save(user);
    }

    @CachePut(value = "users", key = "#userId")
    public void updateUserStatusById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found.")
        );
        switch (user.getStatus()) {
            case UserStatus.STATUS_UNBLOCKED:
                user.setStatus(UserStatus.STATUS_BLOCKED);
                break;
            case UserStatus.STATUS_BLOCKED:
                user.setStatus(UserStatus.STATUS_UNBLOCKED);
                break;
        }
        userRepository.save(user);
    }

    @CachePut(value = "users", key = "#userId")
    public void updateUserVisibilityById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found.")
        );
        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }
        switch (user.getStatus()) {
            case STATUS_ONLINE -> user.setStatus(UserStatus.STATUS_OFFLINE);
            case STATUS_OFFLINE -> user.setStatus(UserStatus.STATUS_ONLINE);
        }
        userRepository.save(user);
    }

    @CachePut(value = "users", key = "#userId")
    public void updateUserPhoneNumberById(Long userId, String phoneNumber) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found.")
        );
        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }
        if (phoneNumber == null || !Objects.equals(phoneNumber, user.getPhoneNumber())) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Phone number " + phoneNumber + " is unavailable.");
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Phone number " + phoneNumber + " is unavailable.");
        }
        user.setPhoneNumber(phoneNumber);
        userRepository.save(user);
    }

    @CacheEvict(value = "users", key = "#userId")
    public void deleteUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id " + userId + " not found.")
        );
        if (user.getCards().isEmpty()) {
            userRepository.delete(user);
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "User with id " + userId + " has cards. Delete the cards to remove user.");
        }
    }
}


