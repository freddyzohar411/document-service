package com.avensys.rts.documentservice.annotation;

import com.avensys.rts.documentservice.validators.FileSizeValidator;
import com.avensys.rts.documentservice.validators.ValidPdfFileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileSizeValidator.class)
public @interface FileSize {
    String message() default "Only files with size less than 5MB are allowed";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    long maxSize() default 1;  // Default is 1 MB
}