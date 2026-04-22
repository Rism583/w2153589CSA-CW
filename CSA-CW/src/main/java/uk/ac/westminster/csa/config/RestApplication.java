/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.westminster.csa.config;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * This class acts as the main entry point for the "Smart Campus" REST API.
 * By extending the JAX-RS Application class, I am bootstrapping the web service 
 * within the Jakarta EE container (GlassFish 7).
 */

@ApplicationPath("/api/v1")
public class RestApplication extends Application{
    /* * I've set the @ApplicationPath to "/api/v1" to establish the versioned 
     * entry point required by the coursework specification.
     * * This class can remain empty because the JAX-RS runtime will automatically 
     * scan my project for classes annotated with @Path to register them 
     * as resources.
     */
}
