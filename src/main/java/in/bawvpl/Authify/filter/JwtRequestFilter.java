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

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // ================= SKIP FILTER =================
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getRequestURI();

        // ✅ Allow CORS preflight
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // ✅ PUBLIC ENDPOINTS (FIXED)
        return path.startsWith("/api/v1.0/auth/")
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

            if (jwt.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            String username;

            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                log.warn("Invalid JWT token: {}", e.getMessage());
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

                    log.debug("Authenticated user: {}", username);

                } else {
                    log.warn("JWT validation failed for user: {}", username);
                }
            }

        } catch (Exception ex) {
            log.error("JWT filter error: {}", ex.getMessage());
        }

        // ✅ Always continue filter chain
        filterChain.doFilter(request, response);
    }
}