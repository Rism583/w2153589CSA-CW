/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.westminster.csa.resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uk.ac.westminster.csa.dao.MockDatabase;
import uk.ac.westminster.csa.models.Room;
import uk.ac.westminster.csa.models.Sensor;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Controller for managing Sensor resources at /api/v1/sensors.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON) // 3.1 explicitly requires this annotation
public class SensorResource {

    // Accessing our thread-safe database
    private final MockDatabase db = MockDatabase.getInstance();

    /**
     * Part 3.2: GET / - Provide a list of sensors, with optional type filtering.
     * Example: GET /api/v1/sensors?type=CO2
     */
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        Collection<Sensor> allSensors = db.getSensors().values();

        // If no query parameter is provided, return everything
        if (type == null || type.trim().isEmpty()) {
            return Response.ok(allSensors).build();
        }

        // If a type IS provided, filter the list using Java Streams
        Collection<Sensor> filteredSensors = allSensors.stream()
                .filter(sensor -> sensor.getType() != null && sensor.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());

        return Response.ok(filteredSensors).build();
    }

    /**
     * Part 3.1: POST / - Register a new sensor with foreign-key integrity checks.
     */
    @POST
    public Response registerSensor(Sensor sensor) {
        // Basic payload validation
        if (sensor.getId() == null || sensor.getRoomId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\":\"Sensor ID and Room ID are required.\"}")
                           .build();
        }

        if (db.getSensors().containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                           .entity("{\"error\":\"Sensor with this ID already exists.\"}")
                           .build();
        }

        // 1. INTEGRITY CHECK: Does the room actually exist in our system?
        Room targetRoom = db.getRooms().get(sensor.getRoomId());
        if (targetRoom == null) {
            // The rubric requires failing if the roomId is fake. 
            // 400 Bad Request is appropriate for validation failures.
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\":\"Cannot register sensor: The specified roomId does not exist.\"}")
                           .build();
        }

        // 2. Save the sensor to the database
        db.getSensors().put(sensor.getId(), sensor);

        // 3. CRITICAL LINKING STEP: Add this sensor's ID to the Room's list.
        // If we forget this, the DELETE safety logic we wrote in Part 2.2 won't work!
        targetRoom.getSensorIds().add(sensor.getId());

        // Standard REST response for creation
        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }
    
    /**
     * Part 4.1: Sub-Resource Locator Pattern.
     * Notice there is NO @GET or @POST here! JAX-RS sees the missing verb 
     * and delegates the entire request to the SensorReadingResource class.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
