package com.example.doc_upload.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Runs once at startup to confirm credentials + bucket access.
 * Uses a 1-item list under our prefix, which the IAM policy from Step 2 allows.
 * (HeadBucket would be denied by that policy, so we avoid it.)
 */
@Component
public class S3ConnectivityCheck implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(S3ConnectivityCheck.class);

    private final S3Client s3;
    private final AwsProperties props;

    public S3ConnectivityCheck(S3Client s3, AwsProperties props) {
        this.s3 = s3;
        this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) {
        String bucket = props.s3().bucket();
        String prefix = props.s3().keyPrefix();
        try {
            var response = s3.listObjectsV2(r -> r.bucket(bucket).prefix(prefix).maxKeys(1));
            log.info("✅ S3 connected: bucket='{}', prefix='{}', region='{}', objects found: {}",
                    bucket, prefix, props.region(), response.keyCount());
        } catch (S3Exception e) {
            // AWS answered, but said no (403 = permissions, 404 = wrong bucket name)
            log.error("❌ S3 rejected the request: {} {} - {}",
                    e.statusCode(), e.awsErrorDetails().errorCode(), e.awsErrorDetails().errorMessage());
        } catch (SdkException e) {
            // Never reached AWS: missing credentials, no network, bad region...
            log.error("❌ Could not reach S3: {}", e.getMessage());
        }
    }
}