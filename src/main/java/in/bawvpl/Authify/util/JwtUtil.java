package in.bawvpl.Authify.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    @Value("${auth.jwt.secret}")
    private String secret;

    @Value("${auth.jwt.expiration}")
    private long expiration;

    // ================= KEY =================
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ================= GENERATE TOKEN =================
    public String generateAccessToken(String email) {

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // (Optional alias)
    public String generateToken(String email) {
        return generateAccessToken(email);
    }

    // ================= EXTRACT USERNAME =================
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    // ================= VALIDATE TOKEN =================
    public boolean validateToken(String token, String username) {

        try {
            if (token == null || token.isBlank()) {
                return false;
            }

            String extractedUsername = extractUsername(token);

            return extractedUsername.equals(username) && !isTokenExpired(token);

        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("JWT malformed: {}", e.getMessage());
        } catch (SignatureException e) {
            log.error("JWT signature invalid: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT empty or null: {}", e.getMessage());
        }

        return false;
    }

    // ================= CHECK EXPIRY =================
    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    // ================= EXTRACT CLAIMS =================
    private Claims extractClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}