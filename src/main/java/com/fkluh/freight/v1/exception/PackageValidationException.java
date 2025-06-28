package com.fkluh.freight.v1.exception;

public class PackageValidationException extends RuntimeException {
    public PackageValidationException(String message) {
        super(message);
    }
}