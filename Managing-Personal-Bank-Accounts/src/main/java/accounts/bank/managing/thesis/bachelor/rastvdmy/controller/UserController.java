package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;


import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.UserRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.User;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        LOG.debug("Getting all users ...");
        return ResponseEntity.ok(userService.getUsers());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/filter")
    public ResponseEntity<Page<User>> filterUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "asc") String sort) {
        LOG.debug("Filtering users ...");
        if (sort.equalsIgnoreCase("asc")) {
            LOG.debug("Sorting users by name in ascending order ...");
            Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
            return ResponseEntity.ok(userService.filterAndSortUsers(pageable));
        } else if (sort.equalsIgnoreCase("desc")) {
            LOG.debug("Sorting users by name in descending order ...");
            Pageable pageable = PageRequest.of(page, size, Sort.by("name").descending());
            return ResponseEntity.ok(userService.filterAndSortUsers(pageable));
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid sort option. Use 'asc' or 'desc'");
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable(value = "id") Long userId) {
        LOG.debug("Getting user id: {} ...", userId);
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/")
    public ResponseEntity<User> createUser(@RequestBody UserRequest user) {
        LOG.debug("Creating user: {} ...", user.name());
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

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PutMapping(path = "/{id}")
    public void updateUserById(@PathVariable(value = "id") Long userId, @RequestBody UserRequest user) {
        LOG.debug("Updating user id: {} ...", userId);
        userService.updateUserById(userId,
                user.email(),
                user.password(),
                user.phoneNumber());
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}/avatar")
    public void uploadUserAvatar(@PathVariable(value = "id") Long userId, @RequestBody MultipartFile userAvatar) {
        LOG.debug("Uploading user avatar id: {} ...", userId);
        userService.uploadUserAvatar(userId, userAvatar);
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}/email")
    public void updateUserEmailById(@PathVariable(value = "id") Long userId, @RequestBody String email) {
        LOG.debug("Updating user email id: {} ...", userId);
        userService.updateUserEmailById(userId, email);
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}/password")
    public void updateUserPasswordById(@PathVariable(value = "id") Long userId, @RequestBody String password) {
        LOG.debug("Updating user password id: {} ...", userId);
        userService.updateUserPasswordById(userId, password);
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}/role")
    public void updateUserRoleById(@PathVariable(value = "id") Long userId, @RequestBody String role) {
        LOG.debug("Updating user role id: {} ...", userId);
        userService.updateUserRoleById(userId, role);
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}/visibility")
    public void updateUserStatusById(@PathVariable(value = "id") Long userId, @RequestBody String status) {
        LOG.debug("Updating user state id: {} ...", userId);
        userService.updateUserStatusById(userId, status);
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}/status")
    public void updateUserStatusById(@PathVariable(value = "id") Long userId) {
        LOG.debug("Updating user status id: {} ...", userId);
        userService.updateUserVisibilityById(userId);
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}/phoneNumber")
    public void updateUserPhoneNumberById(@PathVariable(value = "id") Long userId, @RequestBody String phoneNumber) {
        LOG.debug("Updating user phone number id: {} ...", userId);
        userService.updateUserPhoneNumberById(userId, phoneNumber);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{id}")
    public void deleteUserById(@PathVariable(value = "id") Long userId) {
        LOG.debug("Deleting user by id: {} ...", userId);
        userService.deleteUserById(userId);
    }
}
