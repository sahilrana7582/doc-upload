
package com.example.doc_upload.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


@Validated 
@ConfigurationProperties(prefix = "app.aws")
public record AwsProperties(
        @NotBlank String region,
        @Valid @NotNull S3 s3
) {

    public record S3(
            @NotBlank String bucket,
            String keyPrefix
    ) {
        public S3 {
            if (keyPrefix == null || keyPrefix.isBlank()) {
                keyPrefix = "";
            } else if (!keyPrefix.endsWith("/")) {
                keyPrefix = keyPrefix + "/";
            }
        }
    }
}
