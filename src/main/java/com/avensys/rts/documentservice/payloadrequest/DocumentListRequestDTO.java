package com.avensys.rts.documentservice.payloadrequest;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * author: Koh He Xiang This is the DTO class for a request to list of document
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentListRequestDTO {
	List<DocumentRequestDTO> documentRequestList;
}
