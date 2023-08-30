package com.avensys.rts.documentservice.payload;

//import com.avensys.rts.documentservice.annotation.FileSize;
import com.avensys.rts.documentservice.annotation.FileSize;
import com.avensys.rts.documentservice.annotation.ValidPdfFile;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
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
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequestDTO {
    @NotEmpty(message = "Type cannot be empty")
    private String type;
    private String title;
    private String description;
    private int entityId;

    @NotNull(message = "File cannot be null")
    @ValidPdfFile(message = "File must be a PDF file")
    @FileSize(maxSize = 1, message = "File size must be less than 1MB")
    private MultipartFile file;
}
