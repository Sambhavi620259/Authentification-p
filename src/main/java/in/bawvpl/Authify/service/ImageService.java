package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.ImageEntity;
import in.bawvpl.Authify.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;

    public ImageEntity save(MultipartFile file) throws IOException {
        ImageEntity img = ImageEntity.builder()
                .filename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .data(file.getBytes())
                .build();
        return imageRepository.save(img);
    }

    // helper to save from a local path (useful for your sandbox file)
    public ImageEntity saveFromFilePath(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        ImageEntity img = ImageEntity.builder()
                .filename(path.getFileName().toString())
                .contentType(Files.probeContentType(path)) // might be null; handle if needed
                .data(bytes)
                .build();
        return imageRepository.save(img);
    }

    public ImageEntity findById(Long id) {
        return imageRepository.findById(id).orElse(null);
    }
}
