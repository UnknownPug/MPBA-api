package api.mpba.rastvdmy.service;

import api.mpba.rastvdmy.dto.request.AdminUpdateUserRequest;
import api.mpba.rastvdmy.dto.request.UserUpdateRequest;
import api.mpba.rastvdmy.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service for managing users
 */
public interface UserService {

    /**
     * Get all users
     *
     * @return list of users
     */
    List<User> getUsers();

    /**
     * Get all users with pagination and sorting
     * @param pageable page and sort options
     * @return page of users
     */
    Page<User> filterAndSortUsers(Pageable pageable);

    User getUser(HttpServletRequest request);

    User getUserByEmail(String email);

    User updateUser(HttpServletRequest request, UserUpdateRequest userRequest) throws Exception;

    User updateUserSpecificCredentials(String email, @Valid AdminUpdateUserRequest userRequest);

    void uploadUserAvatar(HttpServletRequest request, MultipartFile userAvatar);

    void removeUserAvatar(HttpServletRequest request);

    void updateUserRole(HttpServletRequest request, String email);

    void updateUserStatus(HttpServletRequest request, String email);

    void deleteUser(HttpServletRequest request);

    void deleteUserById(HttpServletRequest request, String email);

    UserDetailsService userDetailsService();

    String generateToken(User user);
}
