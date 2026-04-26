package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.UserApplicationEntity;
import in.bawvpl.Authify.io.RecentAppDto;
import in.bawvpl.Authify.repository.UserApplicationRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecentAppService {

    private final UserApplicationRepository userAppRepo;

    public List<RecentAppDto> getRecentApps(Long userId) {

        List<UserApplicationEntity> list =
                userAppRepo.findAllByUser_Id(userId);

        return list.stream()
                .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                .limit(5)
                .map(ua -> new RecentAppDto(
                        ua.getApp().getAppId(),
                        ua.getApp().getAppName(),
                        ua.getApp().getAppLogo(),
                        ua.getApp().getAppUrl(),
                        ua.getVisitCounter(),
                        ua.getUpdatedAt()
                ))
                .toList();
    }
}