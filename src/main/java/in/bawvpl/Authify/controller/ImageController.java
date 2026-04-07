package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.ImageEntity;
import in.bawvpl.Authify.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1.0/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    // Upload via multipart/form-data
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            ImageEntity saved = imageService.save(file);
            return ResponseEntity.ok().body(saved.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }

    // Convenience: read that sandbox file and store it in DB (only for local/dev)
    @PostMapping("/upload-from-sandbox")
    public ResponseEntity<?> uploadFromSandbox() {
        try {
            // exact sandbox path you uploaded earlier
            Path path = Paths.get("/mnt/data/Screenshot 2025-11-22 at 3.37.59 PM.png");
            ImageEntity saved = imageService.saveFromFilePath(path);
            return ResponseEntity.ok().body(saved.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload from sandbox failed: " + e.getMessage());
        }
    }

    // Get raw image bytes (browser-friendly)
    @GetMapping("/{id}/raw")
    public ResponseEntity<byte[]> getImageRaw(@PathVariable Long id) {
        ImageEntity img = imageService.findById(id);
        if (img == null) {
            return ResponseEntity.notFound().build();
        }

        String contentType = img.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : img.getContentType();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + img.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(img.getData());
    }
}
