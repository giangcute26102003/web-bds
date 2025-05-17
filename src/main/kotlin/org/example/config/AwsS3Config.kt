package org.example.config

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import org.eclipse.microprofile.config.inject.ConfigProperty
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

@ApplicationScoped
class AwsS3Config {
    @Produces
    @ApplicationScoped
    fun s3Client(
        @ConfigProperty(name = "aws.region", defaultValue = "ap-southeast-1") region: String,
        @ConfigProperty(name = "aws.access.key.id") accessKeyId: String,
        @ConfigProperty(name = "aws.secret.access.key") secretAccessKey: String
    ): S3Client =
        S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            ))
            .build()

}
