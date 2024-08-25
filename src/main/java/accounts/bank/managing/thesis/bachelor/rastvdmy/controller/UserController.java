package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;

import accounts.bank.managing.thesis.bachelor.rastvdmy.controller.mapper.UserMapper;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.UserRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.response.UserResponse;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.User;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/users")
public class UserController {
    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<UserResponse>> getUsers() {
        logInfo("Getting all users ...");
        List<User> users = userService.getUsers();
        List<UserResponse> userResponses = users.stream().map(user -> userMapper.toResponse(
                new UserRequest(
                        user.getName(),
                        user.getSurname(),
                        user.getDateOfBirth(),
                        user.getCountryOrigin(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getPhoneNumber()))
        ).toList();
        return ResponseEntity.ok(userResponses);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/filter", produces = "application/json")
    public ResponseEntity<Page<UserResponse>> filterUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "asc") String sort) {
        logInfo("Filtering users ...");
        PageableState pageableState = parseSortOption(sort.trim().toLowerCase());
        Pageable pageable = switch (pageableState) {
            case ASC -> {
                logInfo("Sorting users by name in ascending order ...");
                yield PageRequest.of(page, size, Sort.by("name").ascending());
            }
            case DESC -> {
                logInfo("Sorting users by name in descending order ...");
                yield PageRequest.of(page, size, Sort.by("name").descending());
            }
        };
        Page<User> users = userService.filterAndSortUsers(pageable);
        Page<UserResponse> userResponses = users.map(user -> userMapper.toResponse(
                new UserRequest(
                        user.getName(),
                        user.getSurname(),
                        user.getDateOfBirth(),
                        user.getCountryOrigin(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getPhoneNumber()))
        );
        return ResponseEntity.ok(userResponses);
    }

    private PageableState parseSortOption(String sortOption) {
        try {
            return PageableState.valueOf(sortOption.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Invalid sort option. Use 'sender' or 'receiver' and 'asc' or 'desc'.");
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_DEFAULT', 'ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/me", produces = "application/json")
    public ResponseEntity<UserResponse> getMyUser(HttpServletRequest request) {
        User user = userService.getUser(request);
        UserResponse userResponse = userMapper.toResponse(
                new UserRequest(
                        user.getName(),
                        user.getSurname(),
                        user.getDateOfBirth(),
                        user.getCountryOrigin(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getPhoneNumber()
                )
        );
        return ResponseEntity.ok(userResponse);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<UserResponse> getUserInfo(@PathVariable("id") UUID id) {
        logInfo("Getting user info ...");
        User user = userService.getUserById(id);
        UserResponse userResponse = userMapper.toResponse(
                new UserRequest(
                        user.getName(),
                        user.getSurname(),
                        user.getDateOfBirth(),
                        user.getCountryOrigin(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getPhoneNumber()
                )
        );
        return ResponseEntity.ok(userResponse);
    }

    @PreAuthorize("hasRole('ROLE_DEFAULT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping(path = "/me", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserResponse> updateUser(HttpServletRequest request,
                                                   @Valid @RequestBody UserRequest userRequest) throws Exception {
        logInfo("Updating user: {} ...", userRequest.name());
        User user = userService.updateUser(request, userRequest);
        UserResponse userResponse = userMapper.toResponse(
                new UserRequest(
                        user.getName(),
                        user.getSurname(),
                        user.getDateOfBirth(),
                        user.getCountryOrigin(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getPhoneNumber()
                )
        );
        return ResponseEntity.ok(userResponse);
    }

    @PreAuthorize("hasRole('ROLE_DEFAULT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping(path = "/me/avatar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> manageUserAvatar(HttpServletRequest request,
                                                 @RequestParam("action") String action,
                                                 @RequestBody(required = false) MultipartFile userAvatar) {
        if ("upload".equalsIgnoreCase(action)) {
            logInfo("Uploading user avatar...");
            userService.uploadUserAvatar(request, userAvatar);
        } else if ("remove".equalsIgnoreCase(action)) {
            logInfo("Deleting user avatar...");
            userService.removeUserAvatar(request);
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid action specified.");
        }
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping(path = "/role/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> updateUserRole(@PathVariable(value = "id") UUID id) {
        logInfo("Granting user new role ...");
        userService.updateUserRole(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping(path = "/status/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> updateUserStatus(@PathVariable("id") UUID id,
                                                 @RequestParam(value = "status") String status) {
        logInfo("Updating user status to: {} ...", status);
        userService.updateUserStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ROLE_DEFAULT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/me")
    public ResponseEntity<Void> deleteMyProfile(HttpServletRequest request) {
        logInfo("Deleting user ...");
        userService.deleteUser(request);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteUserProfile(@PathVariable("id") UUID id) {
        logInfo("Deleting user ...");
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    private void logInfo(String message, Object... args) {
        LOG.info(message, args);
    }
}
