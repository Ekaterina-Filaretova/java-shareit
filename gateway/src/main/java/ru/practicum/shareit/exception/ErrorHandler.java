package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice
public class ErrorHandler extends DefaultResponseErrorHandler {

    @ExceptionHandler({HttpClientErrorException.class,
            HttpStatusCodeException.class,
            HttpServerErrorException.class})
    public ResponseEntity<String> httpClientErrorExceptionHandler(HttpStatusCodeException e) {
        ResponseEntity.BodyBuilder bodyBuilder = ResponseEntity.status(e.getRawStatusCode());
        return bodyBuilder.body(e.getResponseBodyAsString());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleConstraintViolationException(final ConstraintViolationException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleValidationException(final ValidationException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
