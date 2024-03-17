package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.User;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    public User createUser(String name, String surname, Date date, String countryOfOrigin,
                           String email, String password, String phoneNumber) {
        // TODO: complete this method
        // Bind the card to the user
        return null;
    }

    public void updateUserById(Long userId, String email, String password, String phoneNumber) {
        // TODO: complete this method
    }

    public void uploadUserAvatar(Long userId, MultipartFile userAvatar) {
        // TODO: check this method
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User with id: " + userId + " not found")
        );
        user.setAvatar(userAvatar.getOriginalFilename());
        userRepository.save(user);
    }

    public void updateUserEmailById(Long userId, String email) {
        // TODO: check this method
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User with id: " + userId + " not found")
        );
        if (email == null || !Objects.equals(email, user.getEmail())) {
            throw new IllegalArgumentException("Email " + email + " is unavailable");
        }
        List<User> users = userRepository.findAll();
        List<User> sortedUsersByEmail = users
                .stream()
                .filter(mail -> email.equals(user.getEmail())).toList();
        if (sortedUsersByEmail.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Email " + email + " is unavailable");
        }
        user.setEmail(email);
        userRepository.save(user);
    }

    public void updateUserPasswordById(Long userId, String password) {
        // TODO: check this method
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User with id: " + userId + " not found")
        );
        if (password == null || !Objects.equals(password, user.getPassword())) {
            throw new IllegalArgumentException("Password is the same or was not entered");
        }
        user.setPassword(password);
        user.encodePassword(passwordEncoder);
        userRepository.save(user);
    }

    public void updateUserPhoneNumberById(Long userId, String phoneNumber) {
        // TODO: check this method
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User with id: " + userId + " not found")
        );
        if (phoneNumber == null || !Objects.equals(phoneNumber, user.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number " + phoneNumber + " is unavailable");
        }
        List<User> users = userRepository.findAll();
        List<User> usersWithGivenPhoneNumber = users
                .stream()
                .filter(u -> phoneNumber.equals(u.getPhoneNumber()))
                .toList();
        if (usersWithGivenPhoneNumber.isEmpty()) {
            throw new IllegalArgumentException("Phone number " + phoneNumber + " is unavailable");
        }
        user.setPhoneNumber(phoneNumber);
        userRepository.save(user);
    }

    public void deleteUserById(Long userId) {
        // TODO: check this method
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User with id " + userId + " not found.")
        );
        userRepository.delete(user);
    }
}


