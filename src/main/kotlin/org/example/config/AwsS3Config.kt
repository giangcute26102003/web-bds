package org.example.config

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

@ApplicationScoped
class AwsS3Config {
    @Produces
    fun s3Client(): S3Client =
        S3Client.builder()
            .region(Region.AP_SOUTHEAST_1) // đổi theo region bạn dùng
            .build()
}