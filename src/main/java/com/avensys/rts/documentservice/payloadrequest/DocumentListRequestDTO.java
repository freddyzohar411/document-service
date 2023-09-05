package com.avensys.rts.documentservice.payloadrequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * author: Koh He Xiang
 * This is the DTO class for a request to list of document
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentListRequestDTO {
    List<DocumentRequestDTO> documentRequestList;
}
