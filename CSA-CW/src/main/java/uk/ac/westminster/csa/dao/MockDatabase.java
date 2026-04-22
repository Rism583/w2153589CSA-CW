/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.westminster.csa.dao;

import uk.ac.westminster.csa.models.Room;
import uk.ac.westminster.csa.models.Sensor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This acts as the centralized, in-memory data storage for the Smart Campus API.
 * I am using the Singleton pattern to ensure all JAX-RS requests interact with 
 * the exact same database instance.
 */
public class MockDatabase {
    
    // The single instance of our mock database
    private static MockDatabase instance;

    /* * As explained in my report, I am using ConcurrentHashMap instead of standard 
     * HashMap to prevent race conditions and ConcurrentModificationExceptions 
     * when multiple requests try to add or read data at the same time.
     */
    private final Map<String, Room> rooms;
    private final Map<String, Sensor> sensors;

    // Private constructor prevents anyone else from creating a new database
    private MockDatabase() {
        rooms = new ConcurrentHashMap<>();
        sensors = new ConcurrentHashMap<>();
        
        // Optional: We can add one dummy room just to have some initial data
        Room library = new Room("LIB-301", "Library Quiet Study", 50);
        rooms.put(library.getId(), library);
    }

    // This is how our resources will get access to the database safely
    public static synchronized MockDatabase getInstance() {
        if (instance == null) {
            instance = new MockDatabase();
        }
        return instance;
    }

    // Getters for the collections
    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Map<String, Sensor> getSensors() {
        return sensors;
    }
}