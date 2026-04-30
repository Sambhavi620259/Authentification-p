package in.bawvpl.Authify.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class RequestUtil {

    public String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        return (ip == null) ? request.getRemoteAddr() : ip.split(",")[0];
    }

    public String getDevice(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}