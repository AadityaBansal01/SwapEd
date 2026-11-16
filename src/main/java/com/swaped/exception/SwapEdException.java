package com.swaped.exception;

/**
 * Base runtime exception for all SwapEd domain and business logic errors.
 */
public class SwapEdException extends RuntimeException {

    public SwapEdException(String message) {
        super(message);
    }

    public SwapEdException(String message, Throwable cause) {
        super(message, cause);
    }
}
