package in.bawvpl.Authify.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {

    @Value("${auth.jwt.secret}")
    private String secret;

    @Value("${auth.jwt.expiration}")
    private long accessTokenExpiry;

    @Value("${auth.jwt.refresh-expiration:604800000}") // 7 days default
    private long refreshTokenExpiry;

    private Key signingKey;

    // ================= INIT =================
    @PostConstruct
    public void init() {

        if (secret == null || secret.length() < 32) {
            throw new RuntimeException("JWT secret must be at least 32 characters long");
        }

        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ================= ACCESS TOKEN =================
    public String generateAccessToken(String username, Integer tokenVersion) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenVersion", tokenVersion == null ? 0 : tokenVersion);

        return buildToken(username, claims, accessTokenExpiry);
    }

    // ================= REFRESH TOKEN =================
    public String generateRefreshToken(String username) {

        return buildToken(username, new HashMap<>(), refreshTokenExpiry);
    }

    // ================= COMMON BUILDER =================
    private String buildToken(String username, Map<String, Object> claims, long expiry) {

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuer("Authify")                  // 🔥 good practice
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiry))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // ================= BACKWARD SUPPORT =================
    public String generateToken(String username) {
        return generateAccessToken(username, 0);
    }

    // ================= EXTRACT USERNAME =================
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ================= TOKEN VERSION =================
    public Integer extractTokenVersion(String token) {

        try {
            Claims claims = extractAllClaims(token);
            return claims.get("tokenVersion", Integer.class);
        } catch (Exception e) {
            return 0;
        }
    }

    // ================= VALIDATE TOKEN =================
    public boolean validateToken(String token, String username) {

        try {

            if (token == null || token.isBlank()) return false;

            String clean = cleanToken(token);

            Claims claims = extractAllClaims(clean);

            return claims.getSubject().equals(username)
                    && !isTokenExpired(claims);

        } catch (ExpiredJwtException e) {
            log.warn("❌ JWT expired");
        } catch (JwtException | IllegalArgumentException e) {
            log.error("❌ JWT invalid: {}", e.getMessage());
        }

        return false;
    }

    // ================= EXTRACT CLAIM =================
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    // ================= CHECK EXPIRY =================
    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    public boolean isTokenExpired(String token) {
        return isTokenExpired(extractAllClaims(token));
    }

    // ================= CLEAN TOKEN =================
    private String cleanToken(String token) {

        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    // ================= PARSE CLAIMS =================
    private Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(cleanToken(token))
                .getBody();
    }
}