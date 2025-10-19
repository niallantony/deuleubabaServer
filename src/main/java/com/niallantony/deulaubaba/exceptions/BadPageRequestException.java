package com.niallantony.deulaubaba.exceptions;

public class BadPageRequestException extends RuntimeException{
    public BadPageRequestException(String message) {
        super(message);
    }
}
