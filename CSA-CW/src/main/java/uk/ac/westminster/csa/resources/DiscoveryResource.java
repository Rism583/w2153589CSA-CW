/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.westminster.csa.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

        
        

/**
 * This resource acts as the "Home Page" for the API.
 * It is mapped to the root path ("/") relative to the @ApplicationPath ("/api/v1").
 */
@Path("/")
public class DiscoveryResource {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDiscoveryInfo(){
        // I am using a nested Map structure because Jersey-Jackson will 
        // automatically serialize this into a clean, hierarchical JSON object.
        Map<String, Object> discoveryData=new HashMap<>();

        //1. Versioning info
        discoveryData.put("api_name" , "Smart Campus Sensor Management API");
        discoveryData.put("version" , "v1.0");
        discoveryData.put("status" , "ONLINE");
        
        // 2. Administrative contact details
        Map<String, String> contactInfo = new HashMap<>();
        contactInfo.put("developer", "Rismeya Kamalachandran");
        contactInfo.put("role", "Lead Backend Architect");
        contactInfo.put("email" , "w2153589@westminster.ac.uk");
        discoveryData.put("contact", contactInfo);
        
        // 3. Map of primary resource collections (HATEOAS implementation)
        Map<String, String> links = new HashMap<>();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        discoveryData.put("_links", links);
        
        // Return a 200 OK response with the generated JSON body
        return Response.ok(discoveryData).build();
    }
    
    
}
