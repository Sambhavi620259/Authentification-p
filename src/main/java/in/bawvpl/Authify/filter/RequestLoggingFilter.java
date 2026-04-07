package in.bawvpl.Authify.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Collectors;

@Component
@Order(0)
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        log.info("Incoming request -> {} {}", request.getMethod(), request.getRequestURI());
        log.info("Remote addr: {}", request.getRemoteAddr());

        String origin = request.getHeader("Origin");
        log.info("Origin header: {}", origin);

        String auth = request.getHeader("Authorization");
        log.info("Authorization header present? {}", (auth != null && !auth.isEmpty()) ? "YES" : "NO");

        // log Content-Type and all headers (compact)
        log.info("Content-Type: {}", request.getHeader("Content-Type"));
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            String headers = Collections.list(headerNames).stream()
                    .map(h -> h + "=" + request.getHeader(h))
                    .collect(Collectors.joining(", "));
            log.debug("All request headers: {}", headers);
        }

        filterChain.doFilter(request, response);
    }
}
