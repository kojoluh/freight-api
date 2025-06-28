package com.fkluh.freight.v1.exception;

public class PackageAlreadyExistsException extends RuntimeException {
    public PackageAlreadyExistsException(String message) {
        super(message);
    }
}