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
import java.util.function.Function;

/**
 * The UserProfileController handles all operations related to user profiles in the application.
 * It provides endpoints for user management, including creating, updating, retrieving, and deleting user profiles.
 * Access to these endpoints is secured based on user roles.
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/users")
public class UserProfileController {
    private static final Logger LOG = LoggerFactory.getLogger(UserProfileController.class);
    private final UserProfileService userProfileService;
    private final UserProfileMapper userProfileMapper;

    /**
     * Constructor for UserProfileController.
     *
     * @param userProfileService The UserProfileService to be used.
     * @param userProfileMapper  The UserProfileMapper to be used.
     */
    @Autowired
    public UserProfileController(UserProfileService userProfileService, UserProfileMapper userProfileMapper) {
        this.userProfileService = userProfileService;
        this.userProfileMapper = userProfileMapper;
    }

    /**
     * Retrieves all user profiles.
     * Only accessible by users with the ADMIN role.
     *
     * @param request the HTTP request
     * @return a list of UserProfileResponse containing user profile details
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<UserProfileResponse>> getUsers(HttpServletRequest request) {
        logInfo("Getting all users ...");
        List<UserProfile> userProfiles = userProfileService.getUsers(request);
        List<UserProfileResponse> userProfileResponse = userProfiles.stream().map(getUserProfileResponse()
        ).toList();
        return ResponseEntity.ok(userProfileResponse);
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
        return user -> userProfileMapper.toResponse(
                new UserProfileRequest(
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
                )
        );
    }

    /**
     * Filters and sorts user profiles based on the specified parameters.
     * Only accessible by users with the ADMIN role.
     *
     * @param request the HTTP request
     * @param page    the page number to retrieve (default is 0)
     * @param size    the size of the page (default is 10)
     * @param sort    the sorting order can be 'asc' or 'desc' (default is 'asc')
     * @return a paginated list of UserProfileResponse
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/filter", produces = "application/json")
    public ResponseEntity<Page<UserProfileResponse>> filterAndSortUsers(
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
        Page<UserProfile> users = userProfileService.filterAndSortUsers(request, pageable);
        Page<UserProfileResponse> userResponses = users.map(getUserProfileResponse()
        );
        return ResponseEntity.ok(userResponses);
    }

    /**
     * Parses the sorting option from the request.
     *
     * @param sortOption the sort option as a string
     * @return the corresponding PageableState
     * @throws ApplicationException if the sort option is invalid
     */
    private PageableState parseSortOption(String sortOption) {
        try {
            return PageableState.valueOf(sortOption.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Invalid sort option. Use 'sender' or 'receiver' and 'asc' or 'desc'.");
        }
    }

    /**
     * Retrieves the currently authenticated user's profile.
     * Accessible by users with DEFAULT or ADMIN roles.
     *
     * @param request the HTTP request
     * @return the UserProfileResponse of the authenticated user
     */
    @PreAuthorize("hasAnyRole('ROLE_DEFAULT', 'ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/me", produces = "application/json")
    public ResponseEntity<UserProfileResponse> getUser(HttpServletRequest request) {
        UserProfile userProfile = userProfileService.getUser(request);
        UserProfileResponse userProfileResponse = userProfileMapper.toResponse(
                new UserProfileRequest(
                        userProfile.getId(),
                        userProfile.getName(),
                        userProfile.getSurname(),
                        userProfile.getDateOfBirth(),
                        userProfile.getCountryOfOrigin(),
                        userProfile.getEmail(),
                        userProfile.getPassword(),
                        userProfile.getPhoneNumber(),
                        userProfile.getAvatar(),
                        userProfile.getStatus(),
                        userProfile.getRole()
                )
        );
        return ResponseEntity.ok(userProfileResponse);
    }

    /**
     * Retrieves a user profile by its ID.
     * Only accessible by users with the ADMIN role.
     *
     * @param request the HTTP request
     * @param userId  the ID of the user profile to retrieve
     * @return the UserProfileResponse containing the user's details
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<UserProfileResponse> getUserById(HttpServletRequest request,
                                                           @PathVariable("id") UUID userId) {
        logInfo("Getting user info ...");
        UserProfile userProfile = userProfileService.getUserById(request, userId);
        UserProfileResponse userProfileResponse = userProfileMapper.toResponse(
                new UserProfileRequest(
                        userProfile.getId(),
                        userProfile.getName(),
                        userProfile.getSurname(),
                        userProfile.getDateOfBirth(),
                        userProfile.getCountryOfOrigin(),
                        userProfile.getEmail(),
                        userProfile.getPassword(),
                        userProfile.getPhoneNumber(),
                        userProfile.getAvatar(),
                        userProfile.getStatus(),
                        userProfile.getRole()
                )
        );
        return ResponseEntity.ok(userProfileResponse);
    }

    /**
     * Updates the currently authenticated user's profile.
     * Accessible by users with the DEFAULT role.
     *
     * @param request       the HTTP request
     * @param updateRequest the request containing updated user data
     * @return a UserTokenResponse containing the new token for the user
     * @throws Exception if the update process fails
     */
    @PreAuthorize("hasRole('ROLE_DEFAULT')")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(path = "/me", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserTokenResponse> updateUser(HttpServletRequest request,
                                                        @Valid @RequestBody
                                                        UserUpdateRequest updateRequest) throws Exception {
        logInfo("Updating user data ...");
        UserProfile userProfile = userProfileService.updateUser(request, updateRequest);
        return generateUserTokenResponse(userProfile);
    }

    /**
     * Updates specific credentials of a user profile by its ID.
     * Only accessible by users with the ADMIN role.
     *
     * @param request       the HTTP request
     * @param userId        the ID of the user profile to update
     * @param updateRequest the request containing updated user credentials
     * @return a UserTokenResponse containing the new token for the user
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserTokenResponse> updateUserSpecificCredentials(HttpServletRequest request,
                                                                           @PathVariable("id") UUID userId,
                                                                           @Valid @RequestBody
                                                                           AdminUpdateUserRequest updateRequest) {
        logInfo("Updating specific user credentials ...");
        UserProfile userProfile = userProfileService.updateUserSpecificCredentials(request, userId, updateRequest);
        return generateUserTokenResponse(userProfile);
    }

    /**
     * Manages the user avatar by uploading or removing it.
     * Accessible by users with the DEFAULT role.
     *
     * @param request    the HTTP request
     * @param action     the action to perform (upload or remove)
     * @param userAvatar the user avatar image file
     * @return a UserTokenResponse containing the new token for the user
     */
    @PreAuthorize("hasRole('ROLE_DEFAULT')")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(path = "/me/avatar", consumes = "multipart/form-data", produces = "application/json")
    public ResponseEntity<UserTokenResponse> manageUserAvatar(HttpServletRequest request,
                                                              @RequestParam("action") String action,
                                                              @RequestParam(value = "user_avatar", required = false)
                                                              MultipartFile userAvatar) {
        handleUserAvatarAction(request, action, userAvatar);
        UserProfile userProfile = userProfileService.getUser(request);
        return generateUserTokenResponse(userProfile);
    }

    /**
     * Generates a UserTokenResponse containing the user's token.
     * This method is used after updating the user profile to provide the user with a new token.
     * The token is used for authentication and authorization.
     * The token is generated by the userProfileService.
     * The UserTokenResponse is returned as a ResponseEntity with an HTTP status of OK.
     * The response body contains the token.
     *
     * @param userProfile the user profile for which to generate the token
     * @return a ResponseEntity containing the UserTokenResponse
     */
    private ResponseEntity<UserTokenResponse> generateUserTokenResponse(UserProfile userProfile) {
        String token = userProfileService.generateToken(userProfile);
        UserTokenResponse userResponse = new UserTokenResponse(token);
        return ResponseEntity.ok(userResponse);
    }

    /**
     * Handles the user avatar action by uploading or removing the avatar image.
     * The action is specified as a string and can be either 'upload' or 'remove'.
     * If the action is 'upload', the user avatar image is uploaded.
     * If the action is 'removed', the user avatar image is removed.
     * If the action is invalid, an ApplicationException is thrown with a BAD_REQUEST status.
     *
     * @param request    the HTTP request
     * @param action     the action to perform (upload or remove)
     * @param userAvatar the user avatar image file
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
     * Updates the user role by changing the user's role to the specified role.
     * Only accessible by users with the ADMIN role.
     *
     * @param request the HTTP request
     * @param userId  the ID of the user profile to update
     * @return a ResponseEntity with an HTTP status of NO_CONTENT
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping(path = "/role/{id}", produces = "application/json")
    public ResponseEntity<Void> updateUserRole(HttpServletRequest request, @PathVariable("id") UUID userId) {
        logInfo("Updating user role ...");
        userProfileService.updateUserRole(request, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates the user status by changing the user's status to the specified status.
     * Only accessible by users with the ADMIN role.
     *
     * @param request the HTTP request
     * @param userId  the ID of the user profile to update
     * @return a ResponseEntity with an HTTP status of NO_CONTENT
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping(path = "/status/{id}", produces = "application/json")
    public ResponseEntity<Void> updateUserStatus(HttpServletRequest request,
                                                 @PathVariable("id") UUID userId) {
        logInfo("Updating user status ...");
        userProfileService.updateUserStatus(request, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes the currently authenticated user's profile.
     * Accessible by users with the DEFAULT role.
     *
     * @param request the HTTP request
     * @return a ResponseEntity with an HTTP status of NO_CONTENT
     */
    @PreAuthorize("hasRole('ROLE_DEFAULT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/me")
    public ResponseEntity<Void> deleteMyProfile(HttpServletRequest request) {
        logInfo("Deleting user ...");
        userProfileService.deleteUser(request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes a user profile by its ID.
     * Only accessible by users with the ADMIN role.
     *
     * @param request the HTTP request
     * @param userId  the ID of the user profile to delete
     * @return a ResponseEntity with an HTTP status of NO_CONTENT
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteUserProfile(HttpServletRequest request, @PathVariable("id") UUID userId) {
        logInfo("Deleting user ...");
        userProfileService.deleteUserByEmail(request, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Logs informational messages to the console.
     *
     * @param message The message to log.
     * @param args    Optional arguments to format the message.
     */
    private void logInfo(String message, Object... args) {
        LOG.info(message, args);
    }
}
