package com.avensys.rts.documentservice.payloadresponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentNewResponseDTO {
    private Integer id;
    private String type;
    private String title;
    private String description;
    private Integer formId;
    private String submissionData;
    private Integer formSubmissionId;
    private String documentKey;

}
