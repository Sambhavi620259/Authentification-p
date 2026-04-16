package in.bawvpl.Authify.filter;

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
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // ================= PUBLIC PATHS =================
    private static final String[] PUBLIC_PATHS = {
            "/",
            "/error",

            // AUTH
            "/api/v1.0/register",
            "/api/v1.0/login",
            "/api/v1.0/login/verify-otp",
            "/api/v1.0/verify",
            "/api/v1.0/send-otp",
            "/api/v1.0/send-reset-otp",
            "/api/v1.0/reset-password",

            // FILES
            "/uploads",

            // SWAGGER
            "/swagger-ui",
            "/v3/api-docs"
    };

    // ================= SKIP FILTER =================
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getRequestURI();

        // ✅ Allow CORS preflight
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        return Arrays.stream(PUBLIC_PATHS)
                .anyMatch(p -> path.equals(p) || path.startsWith(p + "/"));
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

            String jwt = authHeader.substring(7).trim();

            if (jwt.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            String username = null;

            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                log.warn("JWT parsing failed: {}", e.getMessage());
                filterChain.doFilter(request, response);
                return;
            }

            // ================= AUTH =================
            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    auth.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(auth);

                    log.debug("JWT authenticated: {}", username);

                } else {
                    log.warn("Invalid JWT token for user: {}", username);
                }
            }

        } catch (Exception ex) {
            log.error("JWT filter error: {}", ex.getMessage());
        }

        // ✅ Always continue filter chain
        filterChain.doFilter(request, response);
    }
}