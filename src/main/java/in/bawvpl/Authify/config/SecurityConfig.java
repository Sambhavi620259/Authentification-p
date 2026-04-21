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

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // ✅ IMPORTANT
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
                        .authenticationEntryPoint((req, res, ex1) -> {
                            res.setStatus(401);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"message\":\"Unauthorized\"}");
                        })
                        .accessDeniedHandler((req, res, ex2) -> {
                            res.setStatus(403);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"message\":\"Forbidden\"}");
                        })
                )

                // ================= AUTH =================
                .authorizeHttpRequests(auth -> auth

                        // ✅ Preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ================= PUBLIC =================
                        .requestMatchers(
                                "/",
                                "/error",
                                "/api/v1.0/register",
                                "/api/v1.0/login",
                                "/api/v1.0/login/verify-otp",
                                "/api/v1.0/verify",
                                "/api/v1.0/send-otp",
                                "/api/v1.0/send-reset-otp",
                                "/api/v1.0/reset-password",
                                "/uploads/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // ================= ADMIN =================
                        .requestMatchers(
                                "/api/v1.0/admin/**",
                                "/api/v1.0/kyc/verify/**",
                                "/api/v1.0/kyc/reject/**",
                                "/api/v1.0/kyc/all"
                        ).hasRole("ADMIN")

                        // ================= USER (AUTH REQUIRED) =================
                        .requestMatchers(
                                "/api/v1.0/profile/**",
                                "/api/v1.0/application/**",
                                "/api/v1.0/cart/**",
                                "/api/v1.0/payment/**",
                                "/api/v1.0/dashboard/**",
                                "/api/v1.0/kyc/**"
                        ).authenticated()

                        // ================= FALLBACK =================
                        .anyRequest().authenticated()
                )

                // ================= JWT =================
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
                "http://43.205.116.38:*"
        ));

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        config.setAllowedHeaders(List.of("*"));

        config.setAllowCredentials(true);

        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}