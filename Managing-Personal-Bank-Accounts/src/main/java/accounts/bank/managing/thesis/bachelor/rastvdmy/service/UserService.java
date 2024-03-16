package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.User;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User with id: " + userId + " not found")
        );
    }

    public User createUser(String name, String surname, Date date,
                           String countryOfOrigin,
                           String email,
                           String password,
                           String phoneNumber) {
        User user = new User();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        if (userRepository.existsByNameAndSurname(name, surname)) {
            throw new IllegalArgumentException("User with this name and surname already exists");
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("User with this phone number already exists");
        }
        user.setName(name);
        user.setSurname(surname);
        user.setDateOfBirth(date);
        user.setCountryOrigin(countryOfOrigin);
        user.setEmail(email);
        user.setPassword(password);
        user.encodePassword(passwordEncoder);
        user.setAvatar(null);
        user.setPhoneNumber(phoneNumber);
        return userRepository.save(user);
    }

    public void updateUserById(Long userId, String email, String password, String phoneNumber) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User with id: " + userId + " not found")
        );
        if (email == null || Objects.equals(email, user.getEmail()) || userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email " + email + " is unavailable");
        }
        if (password == null || Objects.equals(password, user.getPassword())) {
            throw new IllegalArgumentException("Password is the same or was not entered");
        }
        if (phoneNumber == null || Objects.equals(phoneNumber, user.getPhoneNumber())
                || userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Phone number " + phoneNumber + " is unavailable");
        }
        user.setEmail(email);
        user.setPassword(password);
        user.encodePassword(passwordEncoder);
        user.setPhoneNumber(phoneNumber);
        userRepository.save(user);
    }

    public void uploadUserAvatar(Long userId, MultipartFile userAvatar) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User with id: " + userId + " not found")
        );
        user.setAvatar(userAvatar.getOriginalFilename());
        userRepository.save(user);
    }

    public void updateUserEmailById(Long userId, String email) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User with id: " + userId + " not found")
        );
        if (email == null || Objects.equals(email, user.getEmail()) || userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email " + email + " is unavailable");
        }
        user.setEmail(email);
        userRepository.save(user);
    }

    public void updateUserPasswordById(Long userId, String password) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User with id: " + userId + " not found")
        );
        if (password == null || Objects.equals(password, user.getPassword())) {
            throw new IllegalArgumentException("Password is the same or was not entered");
        }
        user.setPassword(password);
        userRepository.save(user);
    }

    public void updateUserPhoneNumberById(Long userId, String phoneNumber) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User with id: " + userId + " not found")
        );
        if (phoneNumber == null || Objects.equals(phoneNumber, user.getPhoneNumber())
                || userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Phone number " + phoneNumber + " is unavailable");
        }
        user.setPhoneNumber(phoneNumber);
        userRepository.save(user);
    }

    public void deleteUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User with id " + userId + " not found.")
        );
        userRepository.delete(user);
    }
}


