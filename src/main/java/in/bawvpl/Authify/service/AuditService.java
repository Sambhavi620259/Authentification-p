package in.bawvpl.Authify.service;

import jakarta.servlet.http.HttpServletRequest;

public interface AuditService {

    void log(Long userId, String action, String metadata, HttpServletRequest request);

}