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
import com.lab06.filemanagerservice.service.EmailNotificationClient;
import com.lab06.filemanagerservice.service.FileStorageService;
import com.lab06.filemanagerservice.service.SoapAuthClient;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileStorageService fileStorageService;
    private final SoapAuthClient soapAuthClient;
    private final EmailNotificationClient emailNotificationClient;

    public FileController(FileStorageService fileStorageService,
                          SoapAuthClient soapAuthClient,
                          EmailNotificationClient emailNotificationClient) {
        this.fileStorageService = fileStorageService;
        this.soapAuthClient = soapAuthClient;
        this.emailNotificationClient = emailNotificationClient;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadFile(@RequestHeader("Authorization") String authorization,
                                                     @RequestHeader(value = "X-User-Email", required = false) String userEmail,
                                                     @RequestHeader(value = "X-User-Name", required = false) String userName,
                                                     @RequestParam("file") MultipartFile file) {
        String displayName = StringUtils.hasText(userName) ? userName : "User";
        String originalName = file != null ? file.getOriginalFilename() : "unknown-file";

        try {
            String token = extractToken(authorization);
            if (!soapAuthClient.validateToken(token)) {
                emailNotificationClient.sendFail(userEmail, displayName, originalName, "Invalid or expired token");
                throw new UnauthorizedException("Token buruu esvel huchingui baina.");
            }

            String fileUrl = fileStorageService.uploadFile(file);
            emailNotificationClient.sendSuccess(userEmail, displayName, originalName, fileUrl);
            return ResponseEntity.ok(new UploadResponse("File amjilttai huulagdsan.", fileUrl, file.getOriginalFilename()));
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (Exception ex) {
            emailNotificationClient.sendFail(userEmail, displayName, originalName, ex.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new UploadResponse("File huulah uyd aldaa garlaa.", null, originalName));
        }
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
