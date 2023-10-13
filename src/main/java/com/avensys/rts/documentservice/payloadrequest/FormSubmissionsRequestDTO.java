package com.avensys.rts.documentservice.payloadrequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormSubmissionsRequestDTO {
    private Integer formId;
    private String submissionData;
    private Integer entityId;
    private String entityType;
}
