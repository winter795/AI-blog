package com.personalblog.controller;

import com.personalblog.dto.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class FileController {

    @Value("${blog.upload-path:uploads}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) return Result.error("文件为空");
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
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
