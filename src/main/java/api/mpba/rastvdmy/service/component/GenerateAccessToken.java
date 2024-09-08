// src/main/java/api/mpba/rastvdmy/service/component/GenerateAccessToken.java
package api.mpba.rastvdmy.service.component;

import api.mpba.rastvdmy.entity.AccessToken;
import api.mpba.rastvdmy.entity.User;
import api.mpba.rastvdmy.repository.AccessTokenRepository;
import api.mpba.rastvdmy.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
public class GenerateAccessToken {

    private final JwtService jwtService;
    private final AccessTokenRepository tokenRepository;

    @Autowired
    public GenerateAccessToken(JwtService jwtService, AccessTokenRepository tokenRepository) {
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
    }

    public TokenDetails generate(User user) {
        String token = jwtService.generateToken(user);
        long tokenExpiration = jwtService.getExpirationTime();

        AccessToken accessToken = AccessToken.builder()
                .id(UUID.randomUUID())
                .token(token)
                .user(user)
                .expirationDate(LocalDateTime.now().plus(tokenExpiration, ChronoUnit.MILLIS))
                .build();

        tokenRepository.save(accessToken);
        return new TokenDetails(token, tokenExpiration);
    }

    public record TokenDetails(String token, long tokenExpiration) {}
}