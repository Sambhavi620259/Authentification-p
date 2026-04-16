package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1.0/admin")
@RequiredArgsConstructor


public class AdminUsersController {
    private final UserRepository userRepository;

        // ✅ GET ALL USERS
        @GetMapping("/users")
        public ResponseEntity<?> getAllUsers() {
            return ResponseEntity.ok(userRepository.findAll());
        }
    }

