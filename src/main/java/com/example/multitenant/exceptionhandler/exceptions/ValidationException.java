package com.example.multitenant.exceptionhandler.exceptions;

public class ValidationException extends RuntimeException {

    public ValidationException(String message) { super(message); }
}
