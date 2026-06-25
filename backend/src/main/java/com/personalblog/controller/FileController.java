package com.personalblog.controller;

import com.personalblog.dto.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class FileController {

    private static final Set<String> ALLOWED_EXT = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${blog.upload-path:uploads}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) return Result.error("文件为空");

        // 大小校验
        if (file.getSize() > MAX_SIZE) return Result.error("文件不能超过 5MB");

        // 类型校验
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }
        if (!ALLOWED_EXT.contains(ext)) {
            return Result.error("不支持的文件类型，仅允许: " + String.join(", ", ALLOWED_EXT));
        }

        String filename = UUID.randomUUID().toString().substring(0, 8) + ext;
        String dateDir = LocalDate.now().toString();
        Path dir = Paths.get(uploadPath, dateDir);
        Files.createDirectories(dir);
        file.transferTo(dir.resolve(filename).toFile());

        String url = "/uploads/" + dateDir + "/" + filename;
        return Result.success(Map.of("url", url));
    }
}
