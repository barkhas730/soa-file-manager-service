package com.lab06.filemanagerservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.lab06.filemanagerservice.dto.UploadResponse;
import com.lab06.filemanagerservice.exception.UnauthorizedException;
import com.lab06.filemanagerservice.service.FileStorageService;
import com.lab06.filemanagerservice.service.SoapAuthClient;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileStorageService fileStorageService;
    private final SoapAuthClient soapAuthClient;

    public FileController(FileStorageService fileStorageService, SoapAuthClient soapAuthClient) {
        this.fileStorageService = fileStorageService;
        this.soapAuthClient = soapAuthClient;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadFile(@RequestHeader("Authorization") String authorization,
                                                     @RequestParam("file") MultipartFile file) {
        String token = extractToken(authorization);
        if (!soapAuthClient.validateToken(token)) {
            throw new UnauthorizedException("Token buruu esvel huchingui baina.");
        }

        String fileUrl = fileStorageService.uploadFile(file);
        return ResponseEntity.ok(new UploadResponse("File amjilttai huulagdsan.", fileUrl, file.getOriginalFilename()));
    }

    @GetMapping("/health")
    public String health() {
        return "File manager service ajillaj baina.";
    }

    private String extractToken(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authorization header baihgui baina.");
        }

        return authorization.substring(7);
    }
}
