package api.mpba.rastvdmy.service.impl;

import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.dto.request.UserRequest;
import api.mpba.rastvdmy.entity.User;
import api.mpba.rastvdmy.entity.enums.UserRole;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.UserRepository;
import api.mpba.rastvdmy.service.JwtService;
import api.mpba.rastvdmy.service.UserService;
import api.mpba.rastvdmy.service.component.Generator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.UUID;

//@CacheConfig(cacheNames = {"users"})
@Service
public class UserServiceImpl extends Generator implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder encoder,
                           RestTemplate restTemplate, JwtService jwtService) {
        super(restTemplate);
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public Page<User> filterAndSortUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id.toString())
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist."));
    }

    public User getUser(HttpServletRequest request) {
        return userRepository.findByEmail(request.getUserPrincipal().getName())
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist."));
    }

    private String getUserIdFromToken(HttpServletRequest request) {
        final String token = jwtService.extractToken(request);
        return jwtService.extractSubject(token); //FIXME: Don't forget that this is users' email!
    }

    public User updateUser(HttpServletRequest request, UserRequest userRequest) throws Exception {
        final String userId = getUserIdFromToken(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist."));

        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }

        SecretKey secretKey = EncryptionUtil.getSecretKey();

        String encodedEmail = EncryptionUtil.encrypt(userRequest.email(), secretKey, EncryptionUtil.generateIv());
        String encodedDateOfBirth = EncryptionUtil.encrypt(
                userRequest.dateOfBirth(), secretKey, EncryptionUtil.generateIv()
        );
        String encodedPhoneNumber = EncryptionUtil.encrypt(
                userRequest.phoneNumber(), secretKey, EncryptionUtil.generateIv()
        );

        user.setName(userRequest.name());
        user.setSurname(userRequest.surname());
        user.setDateOfBirth(encodedDateOfBirth);
        if (countryExists(userRequest.countryOfOrigin())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Country does not exist.");
        }
        user.setCountryOrigin(userRequest.countryOfOrigin());
        user.setEmail(encodedEmail);
        user.setPassword(userRequest.password());
        user.encodePassword(encoder);
        user.setPhoneNumber(encodedPhoneNumber);
        return userRepository.save(user);
    }

    public void uploadUserAvatar(HttpServletRequest request, MultipartFile userAvatar) {
        final String userId = getUserIdFromToken(request);

        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist.")
        );

        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }

        if (userAvatar.getContentType() == null || !userAvatar.getContentType().startsWith("image")) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "File must be an image.");
        }
        user.setAvatar(userAvatar.getOriginalFilename());
        userRepository.save(user);
    }

    public void removeUserAvatar(HttpServletRequest request) {
        final String userId = getUserIdFromToken(request);

        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist.")
        );

        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }

        user.setAvatar("https://i0.wp.com/sbcf.fr/wp-content/uploads/2018/03/sbcf-default-avatar.png?ssl=1");
        userRepository.save(user);
    }

    public void updateUserRole(UUID id) {
        User user = userRepository.findById(id.toString()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist.")
        );

        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }

        switch (user.getRole()) {
            case ROLE_DEFAULT -> user.setRole(UserRole.ROLE_ADMIN);
            case ROLE_ADMIN -> user.setRole(UserRole.ROLE_DEFAULT);
        }
        userRepository.save(user);
    }

    public void updateUserStatus(UUID id, String status) {
        User user = userRepository.findById(id.toString()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist.")
        );
        switch (user.getStatus()) {
            case STATUS_DEFAULT, STATUS_UNBLOCKED -> {
                user.setStatus(UserStatus.STATUS_BLOCKED);
                userRepository.save(user);
            }
            case STATUS_BLOCKED -> {
                user.setStatus(UserStatus.STATUS_UNBLOCKED);
                userRepository.save(user);
            }
        }
    }

    public void deleteUser(HttpServletRequest request) {
        final String userId = getUserIdFromToken(request);
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist.")
        );

        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }

        if (user.getBankIdentities().isEmpty()) {
            userRepository.delete(user);
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Make sure to delete all bank accounts first.");
        }
    }

    public void deleteUserById(UUID id) {
        User user = userRepository.findById(id.toString()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist.")
        );
        if (user.getBankIdentities().isEmpty()) {
            userRepository.delete(user);
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Make sure to delete all bank accounts first.");
        }
    }

    public UserDetailsService userDetailsService() {
        return userData -> (UserDetails) userRepository.findByEmail(userData).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist.")
        );
    }
}
