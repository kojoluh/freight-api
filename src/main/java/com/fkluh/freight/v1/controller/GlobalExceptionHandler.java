package com.fkluh.freight.v1.controller;

import com.fkluh.freight.v1.exception.ErrorResponse;
import com.fkluh.freight.v1.exception.PackageAlreadyExistsException;
import com.fkluh.freight.v1.exception.PackageNotFoundException;
import com.fkluh.freight.v1.exception.PackageValidationException;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.MethodNotAllowedException;

@Slf4j
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleException(RuntimeException ex) {

        log.error("An error occurred: {} - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);

        if (ex instanceof PackageNotFoundException) {
            return new ResponseEntity<>(new ErrorResponse("PACKAGE_NOT_FOUND", ex.getMessage()), HttpStatus.NOT_FOUND);
        }
        if (ex instanceof PackageAlreadyExistsException) {
            return new ResponseEntity<>(new ErrorResponse("PACKAGE_ALREADY_EXISTS", ex.getMessage()), HttpStatus.CONFLICT);
        }
        if (ex instanceof PackageValidationException ||
            ex instanceof IllegalArgumentException) {
            return new ResponseEntity<>(new ErrorResponse("VALIDATION_ERROR", ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
        if (ex instanceof MethodNotAllowedException) {
            return new ResponseEntity<>(new ErrorResponse("VALIDATION_ERROR", ex.getMessage()), HttpStatus.METHOD_NOT_ALLOWED);
        }

        return new ResponseEntity<>(new ErrorResponse("INTERNAL_SERVER_ERROR", "An unexpected error occurred."), HttpStatus.INTERNAL_SERVER_ERROR);

    }
}