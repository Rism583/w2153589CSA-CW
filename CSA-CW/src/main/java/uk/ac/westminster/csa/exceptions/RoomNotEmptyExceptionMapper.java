/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.westminster.csa.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Intercepts RoomNotEmptyException and translates it into an HTTP 409 response.
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        // Automatically formats the error into a clean JSON response
        return Response.status(Response.Status.CONFLICT)
                       .entity("{\"error\":\"" + exception.getMessage() + "\"}")
                       .type("application/json")
                       .build();
    }
}
