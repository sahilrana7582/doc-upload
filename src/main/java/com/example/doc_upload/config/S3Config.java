package com.example.doc_upload.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration 
@EnableConfigurationProperties(AwsProperties.class)
public class S3Config {

    /**
     * One thread-safe S3Client for the whole app.
     * Credentials come from the default chain:
     * env vars -> AWS_PROFILE (~/.aws/credentials) -> IAM role (EC2/ECS/EKS).
     * Spring calls close() on shutdown automatically.
     */
    @Bean
    public S3Client s3Client(AwsProperties props) {
        return S3Client.builder()
        .region(Region.of(props.region()))
        .credentialsProvider(DefaultCredentialsProvider.builder().build())
        .build();
    }
}
