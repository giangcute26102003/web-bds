package org.example.api

import org.example.model.Property
import jakarta.annotation.security.RolesAllowed
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import java.time.Instant

@Path("/admin/properties")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
class PropertyResource {

    @GET
    fun list(): List<Property> = Property.listAll()

    @GET
    @Path("/{id}")
    fun get(@PathParam("id") id: Long): Property =
        Property.findById(id) ?: throw NotFoundException()

    @POST
    @Transactional
    fun create(prop: Property): Property {
        prop.persist()
        return prop
    }

    @PUT
    @Path("/{id}")
    @Transactional
    fun update(@PathParam("id") id: Long, prop: Property): Property {
        val entity = Property.findById(id) ?: throw NotFoundException()
        entity.title = prop.title
        entity.description = prop.description
        entity.price = prop.price
        entity.location = prop.location
        entity.updatedAt = Instant.now()
        entity.persist()
        return entity
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    fun delete(@PathParam("id") id: Long) {
        Property.deleteById(id)
    }
}