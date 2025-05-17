package org.example.api

import org.example.model.Property
import org.example.model.PropertyImage
import jakarta.annotation.security.RolesAllowed
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestForm
import org.jboss.resteasy.reactive.multipart.FileUpload
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import jakarta.enterprise.context.ApplicationScoped
import java.nio.file.Files
import javax.imageio.ImageIO

@Path("/admin/properties/{propertyId}/images/upload")
@ApplicationScoped
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)
//@RolesAllowed("ADMIN")
class ImageUploadResource @Inject constructor(
    private val s3Client: S3Client,
    @ConfigProperty(name = "aws.s3.bucket", defaultValue = "web-bds-image")
    private val bucketName: String,
    @ConfigProperty(name = "app.upload.max-size", defaultValue = "5242880") // 5MB default
    private val maxFileSize: Long
) {
    data class UploadResponse(val url: String, val id: Long?)

    // Các định dạng ảnh cho phép
    private val allowedImageTypes: List<String> = listOf(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    )

    @POST
    @Transactional
    fun upload(
        @PathParam("propertyId") propertyId: Long,
        @FormParam("file") fileUpload: FileUpload?
    ): Response {
        try {
            // Kiểm tra null fileUpload
            if (fileUpload == null) {
                return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to "No file uploaded"))
                    .build()
            }

            // Kiểm tra property tồn tại
            val property = Property.findById(propertyId) ?: return Response
                .status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Property not found"))
                .build()

            // Kiểm tra file upload có tồn tại không
            if (fileUpload.fileName().isNullOrEmpty()) {
                return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to "No file uploaded"))
                    .build()
            }

            // Kiểm tra định dạng file
            val contentType = fileUpload.contentType() ?: "application/octet-stream"
            if (!allowedImageTypes.contains(contentType)) {
                return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to "Only image files are allowed"))
                    .build()
            }

            // Kiểm tra kích thước file
            val fileSize = Files.size(fileUpload.filePath())
            if (fileSize > maxFileSize) {
                return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to "File size exceeds the limit"))
                    .build()
            }

            // Kiểm tra xem file có phải là ảnh hợp lệ không
            if (!isValidImage(fileUpload.filePath().toFile())) {
                return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to "Invalid image file"))
                    .build()
            }

            // Tạo key cho S3 object
            val filename = fileUpload.fileName() ?: "image.jpg"
            val safeFilename = filename.replace(Regex("[^A-Za-z0-9._-]"), "_")
            val key = "properties/$propertyId/${UUID.randomUUID()}-$safeFilename"

            // Upload lên S3
            Files.newInputStream(fileUpload.filePath()).use { inputStream ->
                val putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build()

                s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(
                    inputStream,
                    Files.size(fileUpload.filePath())
                ))
            }

            // Tạo URL và lưu thông tin vào database
            val url = "https://$bucketName.s3.amazonaws.com/$key"

            val image = PropertyImage().apply {
                this.property = property
                this.imageUrl = url
                this.isMain = false

                // Nếu chưa có ảnh nào, đặt làm ảnh chính
                val imageCount = PropertyImage.count("property.id", propertyId)
                if (imageCount == 0L) {
                    this.isMain = true
                }

                // Đặt thứ tự cao nhất
                val maxOrder = PropertyImage.find("property.id = ?1 order by sortOrder desc", propertyId)
                    .firstResult()?.sortOrder ?: 0
                this.sortOrder = maxOrder + 1
            }
            image.persist()

            return Response.status(Response.Status.CREATED)
                .entity(UploadResponse(url, image.id))
                .build()
        } catch (e: Exception) {
            e.printStackTrace()
            return Response
                .serverError()
                .entity(mapOf("error" to "Failed to upload image: ${e.message}"))
                .build()
        }
    }

    /**
     * Kiểm tra xem file có phải là ảnh hợp lệ không
     */
    private fun isValidImage(file: java.io.File): Boolean {
        return try {
            val image = ImageIO.read(file)
            image != null
        } catch (e: Exception) {
            false
        }
    }
}