package com.avensys.rts.documentservice.payloadrequest;

import com.avensys.rts.documentservice.annotation.FileSize;
import com.avensys.rts.documentservice.annotation.ValidPdfFile;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * author: Koh He Xiang
 * This is the DTO class for a request to add a document
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDTO {
    private String type;
    private String title;
    private String description;

    @NotNull(message = "File cannot be null")
    @ValidPdfFile(message = "File must be a PDF file")
    @FileSize(maxSize = 1, message = "File size must be less than 1MB")
    private MultipartFile file;
}
