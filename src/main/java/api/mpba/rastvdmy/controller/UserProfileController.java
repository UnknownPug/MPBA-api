package api.mpba.rastvdmy.controller;

import api.mpba.rastvdmy.controller.mapper.UserProfileMapper;
import api.mpba.rastvdmy.dto.request.AdminUpdateUserRequest;
import api.mpba.rastvdmy.dto.request.UserProfileRequest;
import api.mpba.rastvdmy.dto.request.UserUpdateRequest;
import api.mpba.rastvdmy.dto.response.UserProfileResponse;
import api.mpba.rastvdmy.dto.response.UserTokenResponse;
import api.mpba.rastvdmy.entity.UserProfile;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The UserProfileController handles all operations related to user profiles in the application.
 * It provides endpoints for user management, including creating, updating, retrieving, and deleting user profiles.
 * Access to these endpoints is secured based on user roles.
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/users")
public class UserProfileController {
    private final UserProfileService userProfileService;
    private final UserProfileMapper userProfileMapper;

    /**
     * Constructs a UserProfileController with the specified user profile service and user profile mapper.
     *
     * @param userProfileService the user profile service
     * @param userProfileMapper  the user profile mapper
     */
    public UserProfileController(UserProfileService userProfileService, UserProfileMapper userProfileMapper) {
        this.userProfileService = userProfileService;
        this.userProfileMapper = userProfileMapper;
    }

    /**
     * Retrieves all user profiles.
     *
     * @param request the HTTP request
     * @return a list of user profile responses
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<UserProfileResponse>> getUsers(HttpServletRequest request) {
        logInfo("Getting all users ...");
        return ResponseEntity.ok(userProfileService.getUsers(request)
                .stream()
                .map(getUserProfileResponse())
                .collect(Collectors.toList()));
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     *
     * @param request the HTTP request
     * @return the user profile response
     */
    @PreAuthorize("hasAnyRole('ROLE_DEFAULT', 'ROLE_ADMIN')")
    @GetMapping(path = "/me", produces = "application/json")
    public ResponseEntity<UserProfileResponse> getUser(HttpServletRequest request) {
        return ResponseEntity.ok(getUserProfileResponse().apply(userProfileService.getUser(request)));
    }

    /**
     * Retrieves a user profile by user ID.
     *
     * @param request the HTTP request
     * @param userId  the ID of the user
     * @return the user profile response
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<UserProfileResponse> getUserById(HttpServletRequest request,
                                                           @PathVariable("id") UUID userId) {
        logInfo("Getting user info ...");
        return ResponseEntity.ok(getUserProfileResponse().apply(userProfileService.getUserById(request, userId)));
    }

    /**
     * Filters and sorts user profiles.
     *
     * @param request the HTTP request
     * @param page    the page number
     * @param size    the page size
     * @param sort    the sort order
     * @return a page of user profile responses
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(path = "/filter", produces = "application/json")
    public ResponseEntity<Page<UserProfileResponse>> filterAndSortUsers(
            HttpServletRequest request,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "asc") String sort) {
        logInfo("Filtering users ...");
        Pageable pageable = createPageable(page, size, sort);
        Page<UserProfile> users = userProfileService.filterAndSortUsers(request, pageable);
        return ResponseEntity.ok(users.map(getUserProfileResponse()));
    }

    /**
     * Updates the profile of the currently authenticated user.
     *
     * @param request       the HTTP request
     * @param updateRequest the user update request
     * @return the user token response
     * @throws Exception if an error occurs during the update
     */
    @PreAuthorize("hasRole('ROLE_DEFAULT')")
    @PatchMapping(path = "/me", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserTokenResponse> updateUser(HttpServletRequest request,
                                                        @Valid @RequestBody
                                                        UserUpdateRequest updateRequest) throws Exception {
        logInfo("Updating user data ...");
        return generateUserTokenResponse(userProfileService.updateUser(request, updateRequest));
    }

    /**
     * Updates specific credentials of a user by user ID.
     *
     * @param request       the HTTP request
     * @param userId        the ID of the user
     * @param updateRequest the admin update user request
     * @return the user token response
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserTokenResponse> updateUserSpecificCredentials(HttpServletRequest request,
                                                                           @PathVariable("id") UUID userId,
                                                                           @Valid @RequestBody
                                                                           AdminUpdateUserRequest updateRequest) {
        logInfo("Updating specific user credentials ...");
        return generateUserTokenResponse(
                userProfileService.updateUserSpecificCredentials(request, userId, updateRequest));
    }

    /**
     * Manages the avatar of the currently authenticated user.
     *
     * @param request    the HTTP request
     * @param action     the action to perform (upload or remove)
     * @param userAvatar the user avatar file
     * @return the user token response
     */
    @PreAuthorize("hasRole('ROLE_DEFAULT')")
    @PatchMapping(path = "/me/avatar", consumes = "multipart/form-data", produces = "application/json")
    public ResponseEntity<UserTokenResponse> manageUserAvatar(HttpServletRequest request,
                                                              @RequestParam("action") String action,
                                                              @RequestParam(value = "user_avatar", required = false)
                                                              MultipartFile userAvatar) {
        handleUserAvatarAction(request, action, userAvatar);
        return generateUserTokenResponse(userProfileService.getUser(request));
    }

    /**
     * Updates the role of a user by user ID.
     *
     * @param request the HTTP request
     * @param userId  the ID of the user
     * @return a response entity with no content
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(path = "/role/{id}", produces = "application/json")
    public ResponseEntity<Void> updateUserRole(HttpServletRequest request, @PathVariable("id") UUID userId) {
        logInfo("Updating user role ...");
        userProfileService.updateUserRole(request, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates the status of a user by user ID.
     *
     * @param request the HTTP request
     * @param userId  the ID of the user
     * @return a response entity with no content
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(path = "/status/{id}", produces = "application/json")
    public ResponseEntity<Void> updateUserStatus(HttpServletRequest request, @PathVariable("id") UUID userId) {
        logInfo("Updating user status ...");
        userProfileService.updateUserStatus(request, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes the profile of the currently authenticated user.
     *
     * @param request the HTTP request
     * @return a response entity with no content
     */
    @PreAuthorize("hasRole('ROLE_DEFAULT')")
    @DeleteMapping(path = "/me")
    public ResponseEntity<Void> deleteMyProfile(HttpServletRequest request) {
        logInfo("Deleting user ...");
        userProfileService.deleteUser(request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes a user profile by user ID.
     *
     * @param request the HTTP request
     * @param userId  the ID of the user
     * @return a response entity with no content
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteUserProfile(HttpServletRequest request, @PathVariable("id") UUID userId) {
        logInfo("Deleting user ...");
        userProfileService.deleteUserByEmail(request, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns a function that maps a UserProfile object to a UserProfileResponse.
     * This function takes a UserProfile as input and transforms it into a
     * UserProfileRequest object, which is then converted to a UserProfileResponse using the userProfileMapper.
     * This is useful for simplifying the mapping process when retrieving user profiles.
     *
     * @return a Function that takes a UserProfile and returns a UserProfileResponse
     */
    private Function<UserProfile, UserProfileResponse> getUserProfileResponse() {
        return user -> userProfileMapper.toResponse(convertToUserRequest(user));
    }

    /**
     * Generates a user token response.
     *
     * @param userProfile the user profile
     * @return the user token response
     */
    private ResponseEntity<UserTokenResponse> generateUserTokenResponse(UserProfile userProfile) {
        return ResponseEntity.ok(new UserTokenResponse(userProfileService.generateToken(userProfile)));
    }

    /**
     * Converts a UserProfile object to a UserProfileRequest object.
     *
     * @param user the user profile
     * @return the user profile request
     */
    private UserProfileRequest convertToUserRequest(UserProfile user) {
        return new UserProfileRequest(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getDateOfBirth(),
                user.getCountryOfOrigin(),
                user.getEmail(),
                user.getPassword(),
                user.getPhoneNumber(),
                user.getAvatar(),
                user.getStatus(),
                user.getRole()
        );
    }

    /**
     * Creates a pageable object based on the provided parameters.
     *
     * @param page the page number
     * @param size the page size
     * @param sort the sort order
     * @return the pageable object
     */
    private Pageable createPageable(int page, int size, String sort) {
        PageableState pageableState = parseSortOption(sort.trim().toLowerCase());
        return switch (pageableState) {
            case ASC -> PageRequest.of(page, size, Sort.by("name").ascending());
            case DESC -> PageRequest.of(page, size, Sort.by("name").descending());
        };
    }

    /**
     * Parses the sort option and returns the corresponding PageableState.
     *
     * @param sortOption the sort option
     * @return the pageable state
     */
    private PageableState parseSortOption(String sortOption) {
        try {
            return PageableState.valueOf(sortOption.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid sort option. Use 'asc' or 'desc'.");
        }
    }

    /**
     * Handles the user avatar action (upload or remove).
     *
     * @param request    the HTTP request
     * @param action     the action to perform
     * @param userAvatar the user avatar file
     */
    private void handleUserAvatarAction(HttpServletRequest request, String action, MultipartFile userAvatar) {
        switch (action.toLowerCase()) {
            case "upload" -> {
                logInfo("Uploading user avatar...");
                userProfileService.uploadUserAvatar(request, userAvatar);
            }
            case "remove" -> {
                logInfo("Deleting user avatar...");
                userProfileService.removeUserAvatar(request);
            }
            default -> throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid action specified.");
        }
    }

    /**
     * Logs an informational message.
     *
     * @param message the message to log
     */
    private void logInfo(String message) {
        log.info(message);
    }
}
