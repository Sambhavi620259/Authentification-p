package in.bawvpl.Authify.config;

import in.bawvpl.Authify.filter.JwtRequestFilter;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableWebSecurity
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

                // ================= EXCEPTION HANDLING =================
                .exceptionHandling(ex -> ex

                        // 🔐 401 → Unauthorized (no login)
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\":\"Unauthorized\"}");
                        })

                        // 🔐 403 → Forbidden (no permission)
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\":\"Forbidden\"}");
                        })
                )

                // ================= AUTH RULES =================
                .authorizeHttpRequests(auth -> auth

                        // ✅ Preflight (CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ================= PUBLIC =================
                        .requestMatchers(
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

                                // FILE ACCESS
                                "/uploads/**",

                                // SWAGGER
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // ================= USER =================
                        .requestMatchers("/api/v1.0/profile/**").authenticated()

                        // ================= ADMIN ONLY =================
                        .requestMatchers("/api/v1.0/kyc/**").hasRole("ADMIN")

                        // ================= ALL OTHER =================
                        .anyRequest().authenticated()
                )

                // ================= JWT FILTER =================
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ================= CORS CONFIG =================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // ✅ Allow frontend origins
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://43.205.116.38:*"
        ));

        // ✅ Methods
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // ✅ Headers
        config.setAllowedHeaders(List.of("*"));

        // ✅ Allow credentials (JWT)
        config.setAllowCredentials(true);

        // ✅ Allow frontend to read token
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}