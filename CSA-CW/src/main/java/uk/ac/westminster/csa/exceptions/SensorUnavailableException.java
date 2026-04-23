/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.westminster.csa.exceptions;

/**
 * Custom exception thrown when attempting to add a reading to a sensor in MAINTENANCE mode.
 */
public class SensorUnavailableException extends RuntimeException {
    
    public SensorUnavailableException(String message) {
        super(message);
    }
}