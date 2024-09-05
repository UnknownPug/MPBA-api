package api.mpba.rastvdmy.service;

import api.mpba.rastvdmy.dto.request.UserRequest;
import api.mpba.rastvdmy.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

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

    User getUserById(UUID id);

    User updateUser(HttpServletRequest request, UserRequest userRequest) throws Exception;

    void uploadUserAvatar(HttpServletRequest request, MultipartFile userAvatar);

    /**
     * Remove user avatar
     */
    void removeUserAvatar(HttpServletRequest request);

    void updateUserRole(UUID id);

    void updateUserStatus(UUID id, String status);

    /**
     * Delete user
     */
    void deleteUser(HttpServletRequest request);

    void deleteUserById(UUID id);

    /**
     * Get user details service
     * @return user details service
     */
    UserDetailsService userDetailsService();
}
