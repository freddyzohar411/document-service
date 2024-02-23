package com.avensys.rts.documentservice.payloadrequest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * author: Koh He Xiang This is the DTO class for a request to delete a document
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDeleteRequestDTO {
	private String type;
	private int entityId;
}
