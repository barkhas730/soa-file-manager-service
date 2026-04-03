package com.lab06.filemanagerservice.service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class FileStorageService {

    private final String baseUrl;
    private final String uploadDir;
    private final String bucketName;
    private final String bucketEndpoint;
    private final String publicBaseUrl;
    private final String accessKey;
    private final String secretKey;
    private final String region;

    public FileStorageService(
            @Value("${app.base-url:http://localhost:8083}") String baseUrl,
            @Value("${app.upload-dir:uploads}") String uploadDir,
            @Value("${s3.bucket-name:}") String bucketName,
            @Value("${s3.endpoint:}") String bucketEndpoint,
            @Value("${s3.public-base-url:}") String publicBaseUrl,
            @Value("${s3.access.key:}") String accessKey,
            @Value("${s3.secret.key:}") String secretKey,
            @Value("${s3.region:sgp1}") String region) {
        this.baseUrl = baseUrl;
        this.uploadDir = uploadDir;
        this.bucketName = bucketName;
        this.bucketEndpoint = bucketEndpoint;
        this.publicBaseUrl = publicBaseUrl;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
    }

    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File songoogui baina.");
        }

        String safeName = buildFileName(file.getOriginalFilename());

        if (useS3Storage()) {
            return uploadToS3(file, safeName);
        }

        return uploadToLocal(file, safeName);
    }

    private String buildFileName(String originalFilename) {
        String cleanName = originalFilename == null ? "file" : originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        return Instant.now().toEpochMilli() + "_" + UUID.randomUUID() + "_" + cleanName;
    }

    private boolean useS3Storage() {
        return !bucketName.isBlank() && !bucketEndpoint.isBlank() && !accessKey.isBlank() && !secretKey.isBlank();
    }

    private String uploadToLocal(MultipartFile file, String fileName) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);
            Path targetFile = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            return baseUrl + "/files/view/" + fileName;
        } catch (IOException ex) {
            throw new IllegalStateException("File hadgalah ued aldaa garlaa.", ex);
        }
    }

    private String uploadToS3(MultipartFile file, String fileName) {
        try (S3Client s3Client = S3Client.builder()
                .endpointOverride(URI.create(bucketEndpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build()) {

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            return buildPublicFileUrl(fileName);
        } catch (IOException ex) {
            throw new IllegalStateException("Spaces ruu file bairshuulah ued aldaa garlaa.", ex);
        } catch (SdkException ex) {
            throw new IllegalStateException("Spaces upload amjiltgui bolloo. S3 tohirgoo bolon bucket permission-ee shalgana uu.", ex);
        }
    }

    private String buildPublicFileUrl(String fileName) {
        if (!publicBaseUrl.isBlank()) {
            return publicBaseUrl.replaceAll("/$", "") + "/" + fileName;
        }

        return "https://" + bucketName + "." + stripProtocol(bucketEndpoint) + "/" + fileName;
    }

    private String stripProtocol(String url) {
        return url.replaceFirst("^https?://", "").replaceAll("/$", "");
    }
}
