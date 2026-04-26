package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.TransactionEntity;
import in.bawvpl.Authify.io.ApiResponse;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1.0/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    private Long getUserId() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow().getId();
    }

    // ✅ PAGINATION + FILTER
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionEntity>>> getAll(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String status
    ) {

        Long userId = getUserId();

        return ResponseEntity.ok(
                ApiResponse.<Page<TransactionEntity>>builder()
                        .status(200)
                        .message("Transactions fetched")
                        .data(transactionService.getTransactions(userId, page, size, status))
                        .build()
        );
    }

    // ✅ DETAIL
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionEntity>> detail(@PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.<TransactionEntity>builder()
                        .status(200)
                        .message("Transaction detail")
                        .data(transactionService.getDetail(id))
                        .build()
        );
    }
}
