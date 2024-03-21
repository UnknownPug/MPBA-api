package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.*;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final CardService cardService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CardService cardService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cardService = cardService;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found")
        );
    }

    public User createUser(String name, String surname, Date dateOfBirth, String countryOfOrigin,
                           String email, String password, String phoneNumber) {
        if (name.isEmpty() || surname.isEmpty() || countryOfOrigin.isEmpty() ||
                email.isEmpty() || password.isEmpty() || phoneNumber.isEmpty()) {
            throw new ApplicationException(HttpStatus.NO_CONTENT, "All user fields must be filled");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Email " + email + " is unavailable");
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Phone number " + phoneNumber + " is unavailable");
        }
        // Creating user with default role and status
        User user = new User();
        user.setName(name);
        user.setSurname(surname);
        user.setDateOfBirth(dateOfBirth);
        if (countryExists(countryOfOrigin)) {
            user.setCountryOrigin(countryOfOrigin);
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Country " + countryOfOrigin + " does not exist");
        }
        user.setEmail(email);
        user.setPassword(password);
        user.encodePassword(passwordEncoder);
        user.setPhoneNumber(phoneNumber);
        user.setUserRole(UserRole.ROLE_USER);
        user.setStatus(UserStatus.STATUS_ONLINE);
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

    public void updateUserById(Long userId, String email, String password, String phoneNumber) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found")
        );
        if (email == null || password == null || phoneNumber == null) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "All user fields must be filled");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Email " + email + " is unavailable");
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Phone number " + phoneNumber + " is unavailable");
        }
        if (Objects.equals(password, user.getPassword())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Entered password is the same as the old one");
        }
        user.setEmail(email);
        user.setPassword(password);
        user.encodePassword(passwordEncoder);
        user.setPhoneNumber(phoneNumber);
        userRepository.save(user);
    }

    public void uploadUserAvatar(Long userId, MultipartFile userAvatar) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found")
        );
        user.setAvatar(userAvatar.getOriginalFilename());
        userRepository.save(user);
    }

    public void updateUserEmailById(Long userId, String email) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found")
        );
        if (email == null) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Email must be filled");
        }
        if (Objects.equals(email, user.getEmail())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Entered email is the same as the old one");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Email " + email + " is unavailable");
        }
        user.setEmail(email);
        userRepository.save(user);
    }

    public void updateUserPasswordById(Long userId, String password) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found")
        );
        if (password == null) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Password must be filled");
        }
        if (Objects.equals(password, user.getPassword())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Entered password is the same as the old one");
        }
        user.setPassword(password);
        user.encodePassword(passwordEncoder);
        userRepository.save(user);
    }

    public void updateUserRoleById(Long userId, String role) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found")
        );
        UserRole newRole;
        try {
            newRole = UserRole.valueOf(role.toUpperCase());
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Role " + role + " does not exist");
        }
        if (user.getUserRole().equals(newRole)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Entered role is the same as the old one");
        }
        user.setUserRole(newRole);
        userRepository.save(user);
    }

    public void updateUserStateById(Long userId, String status) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found")
        );
        UserStatus newStatus;
        try {
            newStatus = UserStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Status " + status + " does not exist");
        }
        if (user.getStatus().equals(newStatus)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Entered status is the same as the old one");
        }
        user.setStatus(newStatus);
        userRepository.save(user);
    }

    public void updateUserPhoneNumberById(Long userId, String phoneNumber) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User with id: " + userId + " not found")
        );
        if (phoneNumber == null || !Objects.equals(phoneNumber, user.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number " + phoneNumber + " is unavailable");
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Phone number " + phoneNumber + " is unavailable");
        }
        user.setPhoneNumber(phoneNumber);
        userRepository.save(user);
    }

    public void deleteUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User with id " + userId + " not found.")
        );
        if (user.getCards().isEmpty()) {
            userRepository.delete(user);
        } else {
            throw new IllegalArgumentException(
                    "User with id " + userId + " has cards. Delete the cards to remove user.");
        }
    }
}


