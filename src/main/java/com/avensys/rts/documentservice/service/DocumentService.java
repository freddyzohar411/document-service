package com.avensys.rts.documentservice.service;

import com.avensys.rts.documentservice.payload.DocumentRequestDTO;
import com.avensys.rts.documentservice.payload.DocumentResponseDTO;

/**
 * @author Koh He Xiang
 * This interface is used to define the methods for the Currency Service
 */
public interface DocumentService {

    /**
     * This method is used to save document and create a document
     * @param documentRequest
     * @return
     */
    DocumentResponseDTO createDocument(DocumentRequestDTO documentRequest);

}
