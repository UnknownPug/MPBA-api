package api.mpba.rastvdmy.controller;

import api.mpba.rastvdmy.dto.request.UserLoginRequest;
import api.mpba.rastvdmy.dto.request.UserProfileRequest;
import api.mpba.rastvdmy.dto.response.JwtAuthResponse;
import api.mpba.rastvdmy.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling authentication-related requests such as user signup and login.
 * <p>
 * This controller exposes endpoints for user registration and authentication,
 * leveraging the {@link AuthService} to manage the underlying authentication logic.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    /**
     * Constructor for AuthController.
     *
     * @param authService The {@link AuthService} to be used.
     */
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
    @PostMapping(path = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<JwtAuthResponse> authenticate(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        logInfo("Authenticating user ...");
        return ResponseEntity.accepted().body(authService.authenticate(userLoginRequest));
    }

    /**
     * Logs informational messages to the console.
     *
     * @param message The message to log.
     */
    private void logInfo(String message) {
        log.info(message);
    }
}
