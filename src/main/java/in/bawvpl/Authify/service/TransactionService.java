package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.TransactionEntity;
import in.bawvpl.Authify.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public Page<TransactionEntity> getTransactions(
            Long userId,
            int page,
            int size,
            String status
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("paymentDate").descending()
        );

        if (status != null && !status.isBlank()) {
            return transactionRepository.findByUserIdAndStatus(userId, status, pageable);
        }

        return transactionRepository.findByUserId(userId, pageable);
    }

    public TransactionEntity getDetail(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }
}
