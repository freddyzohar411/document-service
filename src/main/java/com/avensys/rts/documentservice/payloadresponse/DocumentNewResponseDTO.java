package com.avensys.rts.documentservice.payloadresponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
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
}
