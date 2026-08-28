package ru.anton.springtests2.exception;

import java.io.Serial;

public class NonRetryableException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public NonRetryableException(String message) {
        super(message);
    }
}
