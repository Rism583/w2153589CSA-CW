/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.westminster.csa.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * A catch-all mapper for any unexpected runtime exceptions.
 * Prevents GlassFish from leaking HTML stack traces to the client.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        // 1. Log the actual scary error to the server console so WE can fix it later
        exception.printStackTrace();

        // 2. Return a safe, generic JSON response to the client
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .entity("{\"error\":\"An unexpected server error occurred. Please try again later.\"}")
                       .type("application/json")
                       .build();
    }
}