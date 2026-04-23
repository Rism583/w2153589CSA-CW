/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.westminster.csa.exceptions;

/**
 * Custom exception thrown when a payload references a parent resource that does not exist.
 */
public class LinkedResourceNotFoundException extends RuntimeException {
    
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
