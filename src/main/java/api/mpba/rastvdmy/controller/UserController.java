package api.mpba.rastvdmy.controller;

import api.mpba.rastvdmy.controller.mapper.UserMapper;
import api.mpba.rastvdmy.dto.request.AdminUpdateUserRequest;
import api.mpba.rastvdmy.dto.request.UserRequest;
import api.mpba.rastvdmy.dto.request.UserUpdateRequest;
import api.mpba.rastvdmy.dto.response.UserResponse;
import api.mpba.rastvdmy.dto.response.UserTokenResponse;
import api.mpba.rastvdmy.entity.User;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.service.UserService;
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

import jakarta.validation.Valid;

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
    public ResponseEntity<List<UserResponse>> getUsers(HttpServletRequest request) {
        logInfo("Getting all users ...");
        List<User> users = userService.getUsers(request);
        List<UserResponse> userResponses = users.stream().map(user -> userMapper.toResponse(
                new UserRequest(
                        user.getId(),
                        user.getName(),
                        user.getSurname(),
                        user.getDateOfBirth(),
                        user.getCountryOrigin(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getPhoneNumber(),
                        user.getAvatar(),
                        user.getStatus(),
                        user.getRole()))
        ).toList();
        return ResponseEntity.ok(userResponses);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/filter", produces = "application/json")
    public ResponseEntity<Page<UserResponse>> filterAndSortUsers(
            HttpServletRequest request,
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
        Page<User> users = userService.filterAndSortUsers(request, pageable);
        Page<UserResponse> userResponses = users.map(user -> userMapper.toResponse(
                new UserRequest(
                        user.getId(),
                        user.getName(),
                        user.getSurname(),
                        user.getDateOfBirth(),
                        user.getCountryOrigin(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getPhoneNumber(),
                        user.getAvatar(),
                        user.getStatus(),
                        user.getRole()))
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
    public ResponseEntity<UserResponse> getUser(HttpServletRequest request) {
        User user = userService.getUser(request);
        UserResponse userResponse = userMapper.toResponse(
                new UserRequest(
                        user.getId(),
                        user.getName(),
                        user.getSurname(),
                        user.getDateOfBirth(),
                        user.getCountryOrigin(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getPhoneNumber(),
                        user.getAvatar(),
                        user.getStatus(),
                        user.getRole()
                )
        );
        return ResponseEntity.ok(userResponse);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<UserResponse> getUserById(HttpServletRequest request, @PathVariable("id") UUID userId) {
        logInfo("Getting user info ...");
        User user = userService.getUserById(request, userId);
        UserResponse userResponse = userMapper.toResponse(
                new UserRequest(
                        user.getId(),
                        user.getName(),
                        user.getSurname(),
                        user.getDateOfBirth(),
                        user.getCountryOrigin(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getPhoneNumber(),
                        user.getAvatar(),
                        user.getStatus(),
                        user.getRole()
                )
        );
        return ResponseEntity.ok(userResponse);
    }

    @PreAuthorize("hasRole('ROLE_DEFAULT')")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(path = "/me", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserTokenResponse> updateUser(HttpServletRequest request,
                                                        @Valid @RequestBody
                                                        UserUpdateRequest updateRequest) throws Exception {
        logInfo("Updating user data ...");
        User user = userService.updateUser(request, updateRequest);
        return generateUserTokenResponse(user);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserTokenResponse> updateUserSpecificCredentials(HttpServletRequest request,
                                                                           @PathVariable("id") UUID userId,
                                                                           @Valid @RequestBody
                                                                           AdminUpdateUserRequest updateRequest) {
        logInfo("Updating specific user credentials ...");
        User user = userService.updateUserSpecificCredentials(request, userId, updateRequest);
        return generateUserTokenResponse(user);
    }

    @PreAuthorize("hasRole('ROLE_DEFAULT')")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(path = "/me/avatar", consumes = "multipart/form-data", produces = "application/json")
    public ResponseEntity<UserTokenResponse> manageUserAvatar(HttpServletRequest request,
                                                              @RequestParam("action") String action,
                                                              @RequestParam(value = "user_avatar", required = false)
                                                              MultipartFile userAvatar) {
        handleUserAvatarAction(request, action, userAvatar);
        User user = userService.getUser(request);
        return generateUserTokenResponse(user);
    }

    private ResponseEntity<UserTokenResponse> generateUserTokenResponse(User user) {
        String token = userService.generateToken(user);
        UserTokenResponse userResponse = new UserTokenResponse(token);
        return ResponseEntity.ok(userResponse);
    }

    private void handleUserAvatarAction(HttpServletRequest request, String action, MultipartFile userAvatar) {
        switch (action.toLowerCase()) {
            case "upload" -> {
                logInfo("Uploading user avatar...");
                userService.uploadUserAvatar(request, userAvatar);
            }
            case "remove" -> {
                logInfo("Deleting user avatar...");
                userService.removeUserAvatar(request);
            }
            default -> throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid action specified.");
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping(path = "/role/{id}", produces = "application/json")
    public ResponseEntity<Void> updateUserRole(HttpServletRequest request,
                                                            @PathVariable("id") UUID userId) {
            logInfo("Updating user role ...");
            userService.updateUserRole(request, userId);
            return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping(path = "/status/{id}", produces = "application/json")
    public ResponseEntity<Void> updateUserStatus(HttpServletRequest request,
                                                 @PathVariable("id") UUID userId) {
        logInfo("Updating user status ...");
        userService.updateUserStatus(request, userId);
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
    public ResponseEntity<Void> deleteUserProfile(HttpServletRequest request, @PathVariable("id") UUID userId) {
        logInfo("Deleting user ...");
        userService.deleteUserByEmail(request, userId);
        return ResponseEntity.noContent().build();
    }

    private void logInfo(String message, Object... args) {
        LOG.info(message, args);
    }
}
