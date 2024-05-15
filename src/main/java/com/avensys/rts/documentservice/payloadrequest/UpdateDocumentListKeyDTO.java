package com.avensys.rts.documentservice.payloadrequest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateDocumentListKeyDTO {
	private String entityType;
	private int entityId;
	DocumentKeyRequestDTO[] documentKeyRequestDTO;
}
