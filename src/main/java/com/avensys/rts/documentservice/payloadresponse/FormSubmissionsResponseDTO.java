package com.avensys.rts.documentservice.payloadresponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormSubmissionsResponseDTO {
    private Integer id;
    private String formId;

    private String submissionData;
    private Integer entityId;
    private String entityType;
}
