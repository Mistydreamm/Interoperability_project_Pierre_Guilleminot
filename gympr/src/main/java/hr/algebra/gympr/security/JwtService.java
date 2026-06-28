package hr.algebra.gympr.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-expiry-ms}")
    private long accessExpiryMs;

    private Algorithm algorithm() {
        return Algorithm.HMAC256(secret);
    }

    public String generateAccessToken(UserDetails userDetails) {
        String role = userDetails.getAuthorities().stream()
            .findFirst()
            .map(GrantedAuthority::getAuthority)
            .orElse("");

        return JWT.create()
            .withSubject(userDetails.getUsername())
            .withClaim("role", role)
            .withIssuedAt(new Date())
            .withExpiresAt(new Date(System.currentTimeMillis() + accessExpiryMs))
            .sign(algorithm());
    }

    public String extractUsername(String token) {
        try {
            return decode(token).getSubject();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public boolean isValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username != null
            && username.equals(userDetails.getUsername())
            && !isExpired(token);
    }

    private boolean isExpired(String token) {
        try {
            Date expiresAt = decode(token).getExpiresAt();
            return expiresAt == null || expiresAt.before(new Date());
        } catch (JWTVerificationException e) {
            return true;
        }
    }

    private DecodedJWT decode(String token) {
        return JWT.require(algorithm()).build().verify(token);
    }
}
