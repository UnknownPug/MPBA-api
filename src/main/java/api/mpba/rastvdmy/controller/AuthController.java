package api.mpba.rastvdmy.controller;

import api.mpba.rastvdmy.dto.request.UserLoginRequest;
import api.mpba.rastvdmy.dto.request.UserProfileRequest;
import api.mpba.rastvdmy.dto.response.JwtAuthResponse;
import api.mpba.rastvdmy.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling authentication-related requests such as user signup and login.
 * <p>
 * This controller exposes endpoints for user registration and authentication,
 * leveraging the {@link AuthService} to manage the underlying authentication logic.
 * </p>
 */
@RestController
@RequestMapping(path = "/api/v1/auth")
public class AuthController {
    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    /**
     * Constructor for AuthController.
     *
     * @param authService The {@link AuthService} to be used.
     */
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint for user signup.
     *
     * @param request The {@link UserProfileRequest} containing user details for signup.
     * @return A {@link ResponseEntity} containing the JWT authentication response.
     * @throws Exception If there is an error during signup.
     */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(path = "/signup", consumes = "application/json", produces = "application/json")
    public ResponseEntity<JwtAuthResponse> signUp(@Valid @RequestBody UserProfileRequest request) throws Exception {
        logInfo("Signing up user ...");
        return ResponseEntity.accepted().body(authService.signUp(request));
    }

    /**
     * Endpoint for user login.
     *
     * @param userLoginRequest The {@link UserLoginRequest} containing user credentials.
     * @return A {@link ResponseEntity} containing the JWT authentication response.
     */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(path = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<JwtAuthResponse> authenticate(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        logInfo("Authenticating user ...");
        return ResponseEntity.accepted().body(authService.authenticate(userLoginRequest));
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
