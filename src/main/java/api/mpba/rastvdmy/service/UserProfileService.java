package api.mpba.rastvdmy.service;

import api.mpba.rastvdmy.dto.request.AdminUpdateUserRequest;
import api.mpba.rastvdmy.dto.request.UserUpdateRequest;
import api.mpba.rastvdmy.entity.UserProfile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing user profiles within the application.
 */
public interface UserProfileService {

    /**
     * Retrieves a list of all user profiles.
     *
     * @param request the HTTP request containing context information
     * @return a list of UserProfile objects representing all users
     */
    List<UserProfile> getUsers(HttpServletRequest request);

    /**
     * Filters and sorts user profiles based on the provided criteria.
     *
     * @param request  the HTTP request containing context information
     * @param pageable the pagination and sorting information
     * @return a page of UserProfile objects that match the filter criteria
     */
    Page<UserProfile> filterAndSortUsers(HttpServletRequest request, Pageable pageable);

    /**
     * Retrieves the user profile of the currently authenticated user.
     *
     * @param request the HTTP request containing context information
     * @return the UserProfile object of the authenticated user
     */
    UserProfile getUser(HttpServletRequest request);

    /**
     * Retrieves a user profile by its unique identifier.
     *
     * @param request the HTTP request containing context information
     * @param userId  the unique identifier of the user
     * @return the UserProfile object corresponding to the specified user ID
     */
    UserProfile getUserById(HttpServletRequest request, UUID userId);

    /**
     * Updates the user profile with the provided information.
     *
     * @param request     the HTTP request containing context information
     * @param userRequest the updated user profile data
     * @return the updated UserProfile object
     * @throws Exception if there is an error while updating the user profile
     */
    UserProfile updateUser(HttpServletRequest request, UserUpdateRequest userRequest) throws Exception;

    /**
     * Updates specific credentials of a user identified by their unique ID.
     *
     * @param request     the HTTP request containing context information
     * @param userId      the unique identifier of the user
     * @param userRequest the updated specific user credentials
     * @return the updated UserProfile object
     */
    UserProfile updateUserSpecificCredentials(HttpServletRequest request, UUID userId,
                                              @Valid AdminUpdateUserRequest userRequest);

    /**
     * Uploads an avatar image for the specified user.
     *
     * @param request    the HTTP request containing context information
     * @param userAvatar the image file to be uploaded as the user's avatar
     */
    void uploadUserAvatar(HttpServletRequest request, MultipartFile userAvatar);

    /**
     * Removes the avatar image of the currently authenticated user.
     *
     * @param request the HTTP request containing context information
     */
    void removeUserAvatar(HttpServletRequest request);

    /**
     * Updates the role of a user identified by their unique ID.
     *
     * @param request the HTTP request containing context information
     * @param userId  the unique identifier of the user
     */
    void updateUserRole(HttpServletRequest request, UUID userId);

    /**
     * Updates the status of a user identified by their unique ID.
     *
     * @param request the HTTP request containing context information
     * @param userId  the unique identifier of the user
     */
    void updateUserStatus(HttpServletRequest request, UUID userId);

    /**
     * Deletes the currently authenticated user's profile.
     *
     * @param request the HTTP request containing context information
     */
    void deleteUser(HttpServletRequest request);

    /**
     * Deletes a user profile identified by their email.
     *
     * @param request the HTTP request containing context information
     * @param userId  the unique identifier of the user to be deleted
     */
    void deleteUserByEmail(HttpServletRequest request, UUID userId);

    /**
     * Retrieves the user details service associated with the user profiles.
     *
     * @return the UserDetailsService instance
     */
    UserDetailsService userDetailsService();

    /**
     * Generates an authentication token for the specified user profile.
     *
     * @param userProfile the user profile for which the token is generated
     * @return the generated token as a String
     */
    String generateToken(UserProfile userProfile);
}
