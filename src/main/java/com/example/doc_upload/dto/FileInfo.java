package com.example.doc_upload.dto;

import java.time.Instant;

public record FileInfo(
        String id,
        String originalFilename,
        String contentType,
        long size,
        Instant uploadedAt
) {}
