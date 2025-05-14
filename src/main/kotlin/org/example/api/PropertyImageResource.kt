package org.example.api

import jakarta.annotation.security.RolesAllowed
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.example.model.PropertyImage


@Path("/admin/properties/{propertyId}/images")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
class PropertyImageResource {

    @GET
    fun list(@PathParam("propertyId") propertyId: Long): List<PropertyImage> {
        return PropertyImage.list("property.id", propertyId)
    }

    @PUT
    @Path("/{imageId}/main")
    @Transactional
    fun setMainImage(
        @PathParam("propertyId") propertyId: Long,
        @PathParam("imageId") imageId: Long
    ): PropertyImage {
        // Bỏ cờ isMain của ảnh cũ
        PropertyImage.update("isMain = false where property.id = ?1", propertyId)
        val img = PropertyImage.findById(imageId) as? PropertyImage ?: throw NotFoundException()
        img.isMain = true
        img.persist()
        return img
    }

    @PUT
    @Path("/{imageId}/order")
    @Transactional
    fun updateOrder(
        @PathParam("imageId") imageId: Long,
        order: Map<String, Int>
    ): PropertyImage {
        val img = PropertyImage.findById(imageId) as? PropertyImage ?: throw NotFoundException()
        img.sortOrder = order["sortOrder"] ?: img.sortOrder
        img.persist()
        return img
    }

    @DELETE
    @Path("/{imageId}")
    @Transactional
    fun delete(
        @PathParam("imageId") imageId: Long
    ) {
        PropertyImage.deleteById(imageId)
    }
}