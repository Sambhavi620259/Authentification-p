package in.bawvpl.Authify.filter;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

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

    // ================= SKIP FILTER =================
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // ✅ Allow CORS preflight
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // ✅ Public endpoints
        return path.startsWith("/api/v1.0/login")
                || path.startsWith("/api/v1.0/register")
                || path.startsWith("/api/v1.0/login/verify-otp")
                || path.startsWith("/api/v1.0/verify")
                || path.startsWith("/api/v1.0/forgot-password")
                || path.startsWith("/api/v1.0/reset-password")
                || path.startsWith("/api/v1.0/payment/verify")
                || path.startsWith("/uploads/")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs/")
                || path.equals("/")
                || path.equals("/error");
    }

    // ================= MAIN FILTER =================
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {

            final String authHeader = request.getHeader("Authorization");

            // ✅ No token → continue
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String jwt = authHeader.substring(7).trim();

            if (jwt.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            String username;

            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                log.warn("❌ Invalid JWT: {}", e.getMessage());
                filterChain.doFilter(request, response);
                return;
            }

            // ✅ Authenticate only if not already authenticated
            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                // 🔥 FETCH USER FROM DB (FOR TOKEN VERSION)
                UserEntity user = userRepository.findByEmailIgnoreCase(username)
                        .orElse(null);

                if (user == null) {
                    log.warn("❌ User not found in DB: {}", username);
                    filterChain.doFilter(request, response);
                    return;
                }

                // ✅ Validate JWT
                if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {

                    // 🔥 TOKEN VERSION CHECK (LOGOUT ALL SUPPORT)
                    Integer tokenVersionInJwt = jwtUtil.extractTokenVersion(jwt);
                    Integer currentVersion = user.getTokenVersion() == null ? 0 : user.getTokenVersion();

                    if (!currentVersion.equals(tokenVersionInJwt)) {
                        log.warn("❌ Token version mismatch (logout-all triggered)");
                        filterChain.doFilter(request, response);
                        return;
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("✅ Authenticated user: {}", username);

                } else {
                    log.warn("❌ JWT validation failed for user: {}", username);
                }
            }

        } catch (Exception ex) {
            log.error("❌ JWT filter error: {}", ex.getMessage(), ex);
        }

        // ✅ Continue filter chain ALWAYS
        filterChain.doFilter(request, response);
    }
}