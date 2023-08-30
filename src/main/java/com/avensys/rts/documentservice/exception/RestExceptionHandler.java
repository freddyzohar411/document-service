package com.avensys.rts.documentservice.exception;

import com.avensys.rts.documentservice.util.ResponseUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Koh He Xiang
 * This class is used to handle all the exceptions thrown by the application.
 * It is annotated with @ControllerAdvice to indicate that it is a global exception handler.
 */
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler  {

    // This handles the incoming JSON validation errors
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String error = "Incorrect JSON request format";
        return ResponseUtil.generateErrorResponse(HttpStatus.BAD_REQUEST, error);
    }

    /**
     * This method is used to handle the validation errors thrown by the @Valid annotation.
     * @param ex
     * @param headers
     * @param status
     * @param request
     * @return
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        BindingResult bindingResult = ex.getBindingResult();
        List<String> errors = new ArrayList<>();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.add(fieldError.getDefaultMessage());
        }

        for (ObjectError globalError : bindingResult.getGlobalErrors()) {
            errors.add(globalError.getDefaultMessage());
        }

        return ResponseUtil.generateErrorResponse(HttpStatus.BAD_REQUEST, errors.toString());
    }

    /**
     * This method is used to handle the missing request parameters.
     * @param ex
     * @param headers
     * @param status
     * @param request
     * @return
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String error = ex.getParameterName() + " parameter is missing";
        return ResponseUtil.generateErrorResponse(HttpStatus.BAD_REQUEST, error);
    }


    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<Object> exception(RuntimeException ex) {
        return ResponseUtil.generateErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseUtil.generateErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

}
