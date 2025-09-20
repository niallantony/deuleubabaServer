package com.niallantony.deulaubaba.exceptions;

import java.io.IOException;

public class InvalidUserDataException extends RuntimeException {
    public InvalidUserDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
