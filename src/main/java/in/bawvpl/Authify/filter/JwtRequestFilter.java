package in.bawvpl.Authify.filter;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.repository.UserSessionRepository;
import in.bawvpl.Authify.util.JwtUtil;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepo;

    // ================= SKIP =================
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getRequestURI();

        return path.startsWith("/api/v1.0/login")
                || path.startsWith("/api/v1.0/register")
                || path.startsWith("/api/v1.0/verify")
                || path.startsWith("/api/v1.0/forgot-password")
                || path.startsWith("/api/v1.0/reset-password")
                || path.startsWith("/api/v1.0/2fa")
                || path.startsWith("/uploads/")
                || path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs");
    }

    // ================= FILTER =================
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        try {

            String header = request.getHeader("Authorization");

            if (header == null || !header.startsWith("Bearer ")) {
                chain.doFilter(request, response);
                return;
            }

            String jwt = header.substring(7).trim();

            String email;
            try {
                email = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                log.warn("❌ Invalid JWT");
                clearContext();
                chain.doFilter(request, response);
                return;
            }

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserEntity user = userRepository.findByEmailIgnoreCase(email).orElse(null);

                if (user == null) {
                    log.warn("❌ User not found");
                    clearContext();
                    chain.doFilter(request, response);
                    return;
                }

                // 🔥 USER STATUS CHECK
                if ("DEACTIVATED".equalsIgnoreCase(user.getUserStatus())) {
                    log.warn("❌ User deactivated");
                    clearContext();
                    chain.doFilter(request, response);
                    return;
                }

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(email);

                // ================= JWT VALID =================
                if (!jwtUtil.validateToken(jwt, email)) {
                    log.warn("❌ JWT invalid");
                    clearContext();
                    chain.doFilter(request, response);
                    return;
                }

                // ================= TOKEN VERSION =================
                Integer tokenVersionInJwt;
                try {
                    tokenVersionInJwt = jwtUtil.extractTokenVersion(jwt);
                } catch (Exception e) {
                    log.warn("❌ Token version missing");
                    clearContext();
                    chain.doFilter(request, response);
                    return;
                }

                Integer currentVersion = user.getTokenVersion() == null ? 0 : user.getTokenVersion();

                if (!currentVersion.equals(tokenVersionInJwt)) {
                    log.warn("❌ Token version mismatch");
                    clearContext();
                    chain.doFilter(request, response);
                    return;
                }

                // ================= SESSION CHECK =================
                boolean sessionActive = sessionRepo
                        .findByTokenAndActiveTrue(jwt)
                        .isPresent();

                if (!sessionActive) {
                    log.warn("❌ Session revoked");
                    clearContext();
                    chain.doFilter(request, response);
                    return;
                }

                // ================= AUTH SET =================
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(auth);

                log.debug("✅ Authenticated: {}", email);
            }

        } catch (Exception e) {
            log.error("❌ JWT filter error", e);
            clearContext();
        }

        chain.doFilter(request, response);
    }

    private void clearContext() {
        SecurityContextHolder.clearContext();
    }
}