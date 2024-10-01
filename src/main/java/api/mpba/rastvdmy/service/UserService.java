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
import java.util.UUID;

/**
 * Service for managing users
 */
public interface UserService {

    List<User> getUsers(HttpServletRequest request);

    Page<User> filterAndSortUsers(HttpServletRequest request, Pageable pageable);

    User getUser(HttpServletRequest request);

    User getUserById(HttpServletRequest request, UUID userId);

    User updateUser(HttpServletRequest request, UserUpdateRequest userRequest) throws Exception;

    User updateUserSpecificCredentials(HttpServletRequest request, UUID userId, @Valid AdminUpdateUserRequest userRequest);

    void uploadUserAvatar(HttpServletRequest request, MultipartFile userAvatar);

    void removeUserAvatar(HttpServletRequest request);

    void updateUserRole(HttpServletRequest request, UUID userId);

    void updateUserStatus(HttpServletRequest request, UUID userId);

    void deleteUser(HttpServletRequest request);

    void deleteUserByEmail(HttpServletRequest request, UUID userId);

    UserDetailsService userDetailsService();

    String generateToken(User user);
}
