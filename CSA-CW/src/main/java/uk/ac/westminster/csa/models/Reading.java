/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.westminster.csa.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Reading {
    
    private String id;
    private String timestamp;
    private double value;

    // Default constructor is REQUIRED by Jackson to convert JSON into this object
    public Reading() {
        // Automatically generate an ID and timestamp when a new reading is created
        this.id = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now().toString();
    }

    public Reading(double value) {
        this(); // Calls the default constructor to set ID and timestamp
        this.value = value;
    }

    // --- Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}