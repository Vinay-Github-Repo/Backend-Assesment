package com.example.backend.assessment.exception;

public class PayloadTooLargeException extends RuntimeException {
    public PayloadTooLargeException(String message) { super(message); }
}
