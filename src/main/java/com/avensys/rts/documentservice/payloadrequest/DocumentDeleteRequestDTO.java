package com.avensys.rts.documentservice.payloadrequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * author: Koh He Xiang
 * This is the DTO class for a request to delete a document
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDeleteRequestDTO {
    private String type;
    private int entityId;
}
