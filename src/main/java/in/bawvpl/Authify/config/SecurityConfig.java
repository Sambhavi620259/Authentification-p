package in.bawvpl.Authify.config;

import in.bawvpl.Authify.filter.JwtRequestFilter;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.*;

import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // ================= CORS =================
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ================= CSRF =================
                .csrf(csrf -> csrf.disable())

                // ================= SESSION =================
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ================= EXCEPTION =================
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"message\":\"Unauthorized\"}");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"message\":\"Forbidden\"}");
                        })
                )

                // ================= AUTHORIZATION =================
                .authorizeHttpRequests(auth -> auth

                        // ✅ Preflight (CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ================= PUBLIC =================
                        .requestMatchers(
                                "/",
                                "/error",

                                "/api/v1.0/register",
                                "/api/v1.0/login",
                                "/api/v1.0/login/verify-otp",
                                "/api/v1.0/verify",
                                "/api/v1.0/forgot-password",
                                "/api/v1.0/reset-password",

                                "/uploads/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // ================= WEBHOOK =================
                        .requestMatchers("/api/v1.0/payment/verify").permitAll()

                        // ================= ADMIN =================
                        // ⚠️ Ensure DB stores ROLE_ADMIN (with prefix)
                        .requestMatchers(
                                "/api/v1.0/admin/**",
                                "/api/v1.0/kyc/verify/**",
                                "/api/v1.0/kyc/reject/**",
                                "/api/v1.0/kyc/all"
                        ).hasRole("ADMIN")

                        // ================= USER FEATURES =================
                        .requestMatchers(
                                "/api/v1.0/profile/**",
                                "/api/v1.0/settings/**",
                                "/api/v1.0/activity/**",
                                "/api/v1.0/favorites/**",
                                "/api/v1.0/notifications/**",
                                "/api/v1.0/tickets/**",
                                "/api/v1.0/application/**",
                                "/api/v1.0/kyc/**"
                        ).authenticated()

                        // ================= FALLBACK =================
                        .requestMatchers("/api/v1.0/**").authenticated()
                )

                // ================= JWT FILTER =================
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ================= CORS =================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://43.205.116.38"
        ));

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // 🔥 FIXED (avoid wildcard security issue)
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type"
        ));

        config.setAllowCredentials(true);

        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}