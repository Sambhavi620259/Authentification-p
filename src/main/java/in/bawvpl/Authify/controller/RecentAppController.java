package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.ApiResponse;
import in.bawvpl.Authify.io.RecentAppDto;
import in.bawvpl.Authify.service.RecentAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1.0/apps")
@RequiredArgsConstructor
public class RecentAppController {

    private final RecentAppService recentAppService;

    @GetMapping("/recent/{userId}")
    public ResponseEntity<ApiResponse<List<RecentAppDto>>> getRecentApps(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                ApiResponse.<List<RecentAppDto>>builder()
                        .status(200)
                        .message("Recent apps fetched")
                        .data(recentAppService.getRecentApps(userId))
                        .build()
        );
    }
}
