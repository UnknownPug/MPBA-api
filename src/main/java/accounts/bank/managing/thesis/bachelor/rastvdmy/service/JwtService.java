package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.BankIdentity;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.function.Function;

public interface JwtService {
    String extractUsername(String token);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    String generateToken(UserDetails userDetails);

    String generateTokenForBankIdentity(BankIdentity bankIdentity);

    long getExpirationTime();

    boolean isTokenValid(String token, UserDetails userDetails);

    String extractToken(HttpServletRequest request);

    String extractId(String token);
}
