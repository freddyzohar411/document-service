package com.avensys.rts.documentservice.validators;


import com.avensys.rts.documentservice.annotation.ValidPdfFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

/**
 * author: Koh He Xiang
 * Valid PDF file validator
 */
public class ValidPdfFileValidator implements ConstraintValidator<ValidPdfFile, MultipartFile> {
    @Override
    public void initialize(ValidPdfFile constraintAnnotation) {
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            // Null or empty file is considered valid here. You can change this behavior if needed.
            return true;
        }

        String contentType = file.getContentType();
        return contentType != null && contentType.equals("application/pdf");
    }
}