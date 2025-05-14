package org.example.api

import org.example.model.Property
import org.example.model.PropertyImage
import jakarta.annotation.security.RolesAllowed
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestForm

import java.io.InputStream
import java.util.*
import jakarta.inject.Inject
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import jakarta.enterprise.context.ApplicationScoped

@Path("/admin/properties/{propertyId}/images/upload")
@ApplicationScoped
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
class ImageUploadResource @Inject constructor (
    private val s3Client: S3Client
)  {
    data class UploadResponse(val url: String, val id: Long?)

    @POST
    @Transactional
    fun upload(
        @PathParam("propertyId") propertyId: Long,
        @RestForm("file") file: InputStream,
        @RestForm("filename") filename: String
    ): Response {
        val property = Property.findById(propertyId) ?: throw NotFoundException()
        val key = "properties/$propertyId/${UUID.randomUUID()}-$filename"
        val putObjectRequest = PutObjectRequest.builder()
            .bucket("web-bds-image")
            .key(key)
            .contentType("image/jpeg")
            .build()

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file, file.available().toLong()))
        val url = "https://web-bds-image.s3.amazonaws.com/$key"

        val image = PropertyImage().apply {
            this.property = property
            this.imageUrl = url
            this.isMain = false
            this.sortOrder = 0
        }
        image.persist()
        return Response.ok(UploadResponse(url, image.id)).build()
    }
}