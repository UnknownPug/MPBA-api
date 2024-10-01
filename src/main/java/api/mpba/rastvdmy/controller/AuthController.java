package api.mpba.rastvdmy.controller;

import api.mpba.rastvdmy.dto.request.UserLoginRequest;
import api.mpba.rastvdmy.dto.request.UserRequest;
import api.mpba.rastvdmy.dto.response.JwtAuthResponse;
import api.mpba.rastvdmy.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(path = "/api/v1/auth")
public class AuthController {
    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(path = "/signup", consumes = "application/json", produces = "application/json")
    public ResponseEntity<JwtAuthResponse> signUp(@Valid @RequestBody UserRequest request) throws Exception {
        logInfo("Signing up user ...");
        return ResponseEntity.ok(authService.signUp(request));
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(path = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<JwtAuthResponse> authenticate(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        logInfo("Authenticating user ...");
        return ResponseEntity.ok(authService.authenticate(userLoginRequest));
    }

    private void logInfo(String message, Object... args) {
        LOG.info(message, args);
    }
}
