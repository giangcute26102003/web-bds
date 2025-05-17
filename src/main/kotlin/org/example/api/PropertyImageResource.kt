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
            val property = Property.findById(propertyId) ?: return Response
                .status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Property not found"))
                .build()

            val images = PropertyImage.list("property.id", propertyId)
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
            // Kiểm tra xem property có tồn tại không
            val property = Property.findById(propertyId) ?: return Response
                .status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Property not found"))
                .build()

            // Lấy ảnh đang cần set main
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

            // Bỏ cờ isMain của ảnh cũ
            PropertyImage.update("isMain = false where property.id = ?1", propertyId)

            // Set ảnh mới làm main
            image.isMain = true
            image.persist()

            return Response.ok(image).build()
        } catch (e: Exception) {
            return Response
                .serverError()
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
            // Lấy ảnh đang cần update
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

            // Cập nhật thứ tự
            image.sortOrder = request.sortOrder
            image.persist()

            return Response.ok(image).build()
        } catch (e: Exception) {
            return Response
                .serverError()
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