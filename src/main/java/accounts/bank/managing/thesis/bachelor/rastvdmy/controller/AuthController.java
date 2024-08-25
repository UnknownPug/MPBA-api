package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.UserLoginRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.UserRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.response.JwtAuthResponse;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<JwtAuthResponse> signUp(@Valid @RequestBody UserRequest request) throws Exception {
        logInfo("Signing up user: {}", request.email());
        return ResponseEntity.ok(authService.signUp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> authenticate(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        logInfo("Authenticating user: {}", userLoginRequest.email());
        return ResponseEntity.ok(authService.authenticate(userLoginRequest));
    }

    private void logInfo(String message, Object... args) {
        LOG.info(message, args);
    }
}
