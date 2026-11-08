package com.example.doc_upload.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.doc_upload.config.AwsProperties;
import com.example.doc_upload.dto.FileInfo;
import com.example.doc_upload.exception.FileStorageException;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service 
public class FileStorageService {
    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    public static final String META_ORIGINAL_NAME = "original-filename";

    private final S3Client s3;
    private final AwsProperties props;

    public FileStorageService(S3Client s3, AwsProperties props) {
        this.s3 = s3;
        this.props = props;
    }

    public FileInfo upload(MultipartFile file){
        String id = UUID.randomUUID().toString();
        String key = keyFor(id);
        String originalName = cleanFilename(file.getOriginalFilename());

        String contentType = file.getContentType() != null
                ? file.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        PutObjectRequest request = PutObjectRequest.builder()
        .bucket(props.s3().bucket())
        .key(key)
        .contentType(contentType)
                        .metadata(Map.of(META_ORIGINAL_NAME,
                        URLEncoder.encode(originalName, StandardCharsets.UTF_8)))
                .build();

        try(InputStream in = file.getInputStream()){
            s3.putObject(request, RequestBody.fromInputStream(in, file.getSize()));
        } catch(IOException e){
            throw new FileStorageException("Could not read the uploaded file", e);
        }

        log.info("Uploaded '{}' ({} bytes) as s3://{}/{}",
                originalName, file.getSize(), props.s3().bucket(), key);

        return new FileInfo(id, originalName, contentType, file.getSize(), Instant.now());
    }

    String keyFor(String id) {
        return props.s3().keyPrefix() + id;
    }

    private static String cleanFilename(String name) {
        if (name == null || name.isBlank()) {
            return "unnamed";
        }
        String normalized = name.replace('\\', '/');
        String base = normalized.substring(normalized.lastIndexOf('/') + 1).strip();
        return base.isEmpty() ? "unnamed" : base;
    }
}
