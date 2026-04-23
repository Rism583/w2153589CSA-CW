/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.westminster.csa.exceptions;

/**
 * Custom exception thrown when attempting to delete a room that contains sensors.
 */
public class RoomNotEmptyException extends RuntimeException {
    
    public RoomNotEmptyException(String message){
        super(message);
    }
    
}
