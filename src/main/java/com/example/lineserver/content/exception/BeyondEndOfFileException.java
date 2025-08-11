package com.example.lineserver.content.exception;

public class BeyondEndOfFileException extends RuntimeException {
    public BeyondEndOfFileException(String message) {
        super(message);
    }
}
