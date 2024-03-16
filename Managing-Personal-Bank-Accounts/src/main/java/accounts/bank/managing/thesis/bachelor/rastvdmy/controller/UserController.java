package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;


import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.UserRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.User;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/profile")
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/")
    public ResponseEntity<List<User>> getUsers() {
        LOG.info("Get all users");
        return ResponseEntity.ok(userService.getUsers());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable(value = "id") Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Set user id properly.");
        }
        LOG.info("Get user by id: {}", userId);
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/")
    public ResponseEntity<User> createUser(@RequestBody UserRequest user) {
        LOG.info("Create user: {}", user.name());
        if (user.name() == null || user.surname() == null ||
                user.dateOfBirth() == null || user.countryOfOrigin() == null ||
                user.email() == null || user.password() == null || user.phoneNumber() == null) {
            throw new IllegalArgumentException("User data must be fully completed.");
        }
        return ResponseEntity.ok(userService.createUser(
                user.name(),
                user.surname(),
                user.dateOfBirth(),
                user.countryOfOrigin(),
                user.email(),
                user.password(),
                user.phoneNumber()
        ));
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(path = "/{id}")
    public void updateUserById(@PathVariable(value = "id") Long userId, @RequestBody UserRequest user) {
        userService.updateUserById(userId,
                user.email(),
                user.password(),
                user.phoneNumber());
        LOG.info("Update user by id: {}", userId);
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}/avatar")
    public void uploadUserAvatar(@PathVariable(value = "id") Long userId, @RequestBody MultipartFile userAvatar) {
        LOG.info("Upload user avatar by id: {}", userId);
        userService.uploadUserAvatar(userId, userAvatar);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(path = "/{id}/email")
    public void updateUserEmailById(@PathVariable(value = "id") Long userId, @RequestBody String email) {
        userService.updateUserEmailById(userId, email);
        LOG.info("Update user email by id: {}", userId);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(path = "/{id}/password")
    public void updateUserPasswordById(@PathVariable(value = "id") Long userId, @RequestBody String password) {
        userService.updateUserPasswordById(userId, password);
        LOG.info("Update user password by id: {}", userId);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(path = "/{id}/phoneNumber")
    public void updateUserPhoneNumberById(@PathVariable(value = "id") Long userId, @RequestBody String phoneNumber) {
        userService.updateUserPhoneNumberById(userId, phoneNumber);
        LOG.info("Update user phone number by id: {}", userId);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(path = "/{id}")
    public void deleteUserById(@PathVariable(value = "id") Long userId) {
        LOG.info("Delete user by id: {}", userId);
        userService.deleteUserById(userId);
    }
}
