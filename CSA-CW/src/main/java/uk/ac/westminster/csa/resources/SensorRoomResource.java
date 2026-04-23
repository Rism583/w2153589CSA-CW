/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.westminster.csa.resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uk.ac.westminster.csa.dao.MockDatabase;
import uk.ac.westminster.csa.exceptions.RoomNotEmptyException;
import uk.ac.westminster.csa.models.Room;

/**
 * Controller for managing Room resources at /api/v1/rooms.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    // Accessing our thread-safe singleton database
    private final MockDatabase db = MockDatabase.getInstance();

    /**
     * Part 2.1: GET / - Provide a comprehensive list of all rooms.
     */
    @GET
    public Response getAllRooms() {
        
        
        // Returning the values from the ConcurrentHashMap.
        // Jackson will automatically serialize this Collection into a JSON array.
        return Response.ok(db.getRooms().values()).build();
    }

    /**
     * Part 2.1: GET /{roomId} - Fetch detailed metadata for a specific room.
     */
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = db.getRooms().get(roomId);
        
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"error\":\"Room not found\"}")
                           .build();
        }
        return Response.ok(room).build();
    }

    /**
     * Part 2.1: POST / - Enable creation of new rooms.
     */
    @POST
    public Response createRoom(Room room) {
        // Basic validation
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\":\"Room ID is required\"}")
                           .build();
        }

        // Checking if room already exists
        if (db.getRooms().containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                           .entity("{\"error\":\"Room with this ID already exists\"}")
                           .build();
        }

        db.getRooms().put(room.getId(), room);
        
        // The spec requires "appropriate feedback upon success". 
        // 201 Created is the standard RESTful response for a successful POST.
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    /**
     * Part 2.2: DELETE /{roomId} - Allow room decommissioning with safety logic.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = db.getRooms().get(roomId);
        
        // 1. Check if the room exists
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"error\":\"Room not found\"}")
                           .build();
        }

        // 2. CHECK RULE: Are there sensors in the room?
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Cannot delete room: It is currently occupied by active hardware sensors.");
        }

        // 3. Safe to delete
        db.getRooms().remove(roomId);
        
        // 204 No Content is the standard response for successful deletion 
        // when there is no response body to return.
        return Response.noContent().build();
    }
}
