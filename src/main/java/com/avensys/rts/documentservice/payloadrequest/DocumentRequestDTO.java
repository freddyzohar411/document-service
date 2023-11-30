package com.avensys.rts.documentservice.payloadrequest;

import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * author: Koh He Xiang This is the DTO class for a request to add a document
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequestDTO {
	private Integer id;
	private String type;
	private String title;
	private String description;
	private Long createdBy;
	private Long updatedBy;

	@NotNull
	private Integer entityId;

	@NotEmpty
	@Length(max = 20)
	private String entityType;

//    @NotNull(message = "File cannot be null")
//    @ValidPdfFile(message = "File must be a PDF file")
//    @ValidFileFormat(format = {"pdf", "doc", "docx"}, message = "File must be a PDF, doc or docx file")
//    @FileSize(maxSize = 2, message = "File size must be less than 2MB")
	private MultipartFile file;

	private Integer formId;
	private String formData;
}
