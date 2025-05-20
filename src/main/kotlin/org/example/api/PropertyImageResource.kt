package org.example.api

import jakarta.annotation.security.RolesAllowed
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.example.model.Property
import org.example.model.PropertyImage

@Path("/admin/properties/{propertyId}/images")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
class PropertyImageResource {

    @GET
    fun list(@PathParam("propertyId") propertyId: Long): Response {
        try {
            // Kiểm tra xem property có tồn tại không
            val property = Property.findById(propertyId)
                ?: return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(mapOf("error" to "Property not found"))
                    .build()

            // Lấy danh sách ảnh và chuyển sang DTO để tránh vòng lặp
            val images = PropertyImage
                .list("property.id", propertyId)
                .map { image ->
                    image.id?.let {
                        PropertyImageDTO(
                            id = it,
                            isMain = image.isMain,
                            url = image.imageUrl // thay thế bằng tên field thật trong entity của bạn
                        )
                    }
                }

            return Response.ok(images).build()

        } catch (e: Exception) {
            return Response
                .serverError()
                .entity(mapOf("error" to "Failed to fetch images: ${e.message}"))
                .build()
        }
    }


    @PUT
    @Path("/{imageId}/main")
    @Transactional
    fun setMainImage(
        @PathParam("propertyId") propertyId: Long,
        @PathParam("imageId") imageId: Long
    ): Response {
        try {
            // Tìm property
            val property = Property.findById(propertyId)
                ?: return Response.status(Response.Status.NOT_FOUND)
                    .entity(mapOf("error" to "Property not found"))
                    .build()

            // Tìm ảnh
            val image = PropertyImage.findById(imageId)
                ?: return Response.status(Response.Status.NOT_FOUND)
                    .entity(mapOf("error" to "Image not found"))
                    .build()

            // Ảnh không thuộc property này
            if (image.property.id != propertyId) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to "Image does not belong to this property"))
                    .build()
            }

            // Unset isMain của tất cả ảnh thuộc property này
            PropertyImage.update("isMain = false WHERE property.id = ?1", propertyId)

            // Set ảnh được chọn thành main
            image.isMain = true
            image.persist()

            // Trả về DTO để tránh vòng lặp JSON
            val dto = image.id?.let {
                PropertyImageDTO(
                    id = it,
                    url = image.imageUrl,
                    isMain = image.isMain
                )
            }

            return Response.ok(dto).build()

        } catch (e: Exception) {
            return Response.serverError()
                .entity(mapOf("error" to "Failed to set main image: ${e.message}"))
                .build()
        }
    }

    data class OrderRequest(val sortOrder: Int)
    @PUT
    @Path("/{imageId}/order")
    @Transactional
    fun updateOrder(
        @PathParam("propertyId") propertyId: Long,
        @PathParam("imageId") imageId: Long,
        request: OrderRequest
    ): Response {
        try {
            val image = PropertyImage.findById(imageId)
                ?: return Response.status(Response.Status.NOT_FOUND)
                    .entity(mapOf("error" to "Image not found"))
                    .build()

            if (image.property.id != propertyId) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to "Image does not belong to this property"))
                    .build()
            }

            image.sortOrder = request.sortOrder
            image.persist()

            return Response.ok(image).build()

        } catch (e: Exception) {
            return Response.serverError()
                .entity(mapOf("error" to "Failed to update image order: ${e.message}"))
                .build()
        }
    }

    @DELETE
    @Path("/{imageId}")
    @Transactional
    fun delete(
        @PathParam("propertyId") propertyId: Long,
        @PathParam("imageId") imageId: Long
    ): Response {
        try {
            // Lấy ảnh đang cần xóa
            val image = PropertyImage.findById(imageId)
                ?: return Response.status(Response.Status.NOT_FOUND)
                    .entity(mapOf("error" to "Image not found"))
                    .build()

            // Kiểm tra xem ảnh có thuộc property này không
            if (image.property.id != propertyId) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to "Image does not belong to this property"))
                    .build()
            }

            // Xóa ảnh
            PropertyImage.deleteById(imageId)

            // Có thể bổ sung code xóa file ảnh từ S3 ở đây

            return Response.noContent().build()
        } catch (e: Exception) {
            return Response
                .serverError()
                .entity(mapOf("error" to "Failed to delete image: ${e.message}"))
                .build()
        }
    }
}
data class PropertyImageDTO(
    val id: Long,
    val url: String,
    val isMain: Boolean
)
