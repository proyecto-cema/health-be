package com.cema.health.controllers.handlers;

import com.cema.health.domain.ErrorResponse;
import com.cema.health.exceptions.AlreadyExistsException;
import com.cema.health.exceptions.NotFoundException;
import com.cema.health.exceptions.UnauthorizedException;
import com.cema.health.exceptions.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class CemaExceptionHandler {

    @ExceptionHandler(AlreadyExistsException.class)
    public final ResponseEntity<Object> handleAlreadyExistsException(AlreadyExistsException ex, WebRequest request) {

        ErrorResponse error = new ErrorResponse(ex.getMessage(), request.toString());
        return new ResponseEntity(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NotFoundException.class)
    public final ResponseEntity<Object> handleNotFoundException(NotFoundException ex, WebRequest request) {

        ErrorResponse error = new ErrorResponse(ex.getMessage(), request.toString());
        return new ResponseEntity(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public final ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException ex, WebRequest request) {

        ErrorResponse error = new ErrorResponse(ex.getMessage(), request.toString());
        return new ResponseEntity(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public final ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {

        ErrorResponse error = new ErrorResponse(ex.getMessage(), request.toString());
        return new ResponseEntity(error, HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public final ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        String message = "Missing or incorrect fields";
        ErrorResponse error = new ErrorResponse(message, request.toString());

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            error.getViolations().add(
                    new ErrorResponse.Violation(fieldError.getField(), fieldError.getDefaultMessage()));
        }
        return new ResponseEntity(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ValidationException.class)
    public final ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), request.toString());
        return new ResponseEntity(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
