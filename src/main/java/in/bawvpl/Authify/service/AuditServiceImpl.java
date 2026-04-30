package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.AuditLog;
import in.bawvpl.Authify.repository.AuditLogRepository;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void log(Long userId, String action, String metadata, HttpServletRequest request) {

        try {

            String ip = getClientIp(request);
            String device = request.getHeader("User-Agent");

            AuditLog logEntity = AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .metadata(metadata)
                    .ip(ip)
                    .device(device)
                    .build();

            auditLogRepository.save(logEntity);

        } catch (Exception e) {
            log.error("❌ Audit logging failed: {}", e.getMessage());
        }
    }

    // ================= HELPER =================
    private String getClientIp(HttpServletRequest request) {

        String xfHeader = request.getHeader("X-Forwarded-For");

        if (xfHeader == null || xfHeader.isBlank()) {
            return request.getRemoteAddr();
        }

        return xfHeader.split(",")[0];
    }
}