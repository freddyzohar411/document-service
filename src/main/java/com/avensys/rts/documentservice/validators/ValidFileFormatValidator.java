package com.avensys.rts.documentservice.validators;


import com.avensys.rts.documentservice.annotation.ValidFileFormat;
import com.avensys.rts.documentservice.annotation.ValidPdfFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * author: Koh He Xiang
 * Valid file format validator
 */
public class ValidFileFormatValidator implements ConstraintValidator<ValidFileFormat, MultipartFile> {

    private String[] format;

    @Override
    public void initialize(ValidFileFormat constraintAnnotation) {
        this.format = constraintAnnotation.format();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            // Null or empty file is considered valid here. You can change this behavior if needed.
            return true;
        }

        String contentType = file.getContentType();

        // Get extension of file even when there are multiple . in the file name
        String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        List<String> fileExtensionList = Arrays.asList(format);

        return fileExtensionList.contains(fileExtension);
    }
}