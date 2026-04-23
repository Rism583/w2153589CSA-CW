/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.westminster.csa.resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uk.ac.westminster.csa.dao.MockDatabase;
import uk.ac.westminster.csa.exceptions.SensorUnavailableException;
import uk.ac.westminster.csa.models.Reading;
import uk.ac.westminster.csa.models.Sensor;

/**
 * Controller for managing historical readings.
 * CRITICAL ARCHITECTURE NOTE: There is no @Path annotation at the top of this class! 
 * The URL path was already resolved by the locator in SensorResource.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final MockDatabase db = MockDatabase.getInstance();

    // The locator passes the ID from the URL directly into this constructor
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * Part 4.2: GET / - Fetch historical data for this specific sensor.
     */
    @GET
    public Response getReadings() {
        Sensor sensor = db.getSensors().get(sensorId);
        
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"error\":\"Sensor not found\"}")
                           .build();
        }
        
        // Return the thread-safe history list
        return Response.ok(sensor.getReadingHistory()).build();
    }

    /**
     * Part 4.2: POST / - Append a new reading and trigger the Side Effect.
     */
    @POST
    public Response addReading(Reading reading) {
        Sensor sensor = db.getSensors().get(sensorId);
        
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"error\":\"Sensor not found\"}")
                           .build();
        }
        
        // STATE CONSTRAINT CHECK: Is the sensor in MAINTENANCE mode?
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Cannot accept readings: Sensor is currently in MAINTENANCE mode.");
        }

        // 1. Append the new reading to the history list
        sensor.getReadingHistory().add(reading);

        // 2. THE SIDE EFFECT (Part 4.2 Constraint): 
        // Update the parent sensor's currentValue so the dashboard stays updated.
        sensor.setCurrentValue(reading.getValue());

        // 201 Created is the standard response
        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}