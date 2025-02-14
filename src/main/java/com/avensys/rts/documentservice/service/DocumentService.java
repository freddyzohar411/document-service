package com.avensys.rts.documentservice.service;

import com.avensys.rts.documentservice.payloadrequest.DocumentDeleteRequestDTO;
import com.avensys.rts.documentservice.payloadrequest.DocumentRequestDTO;
import com.avensys.rts.documentservice.payloadrequest.UpdateDocumentListKeyDTO;
import com.avensys.rts.documentservice.payloadresponse.DocumentDownloadResponseDTO;
import com.avensys.rts.documentservice.payloadresponse.DocumentNewResponseDTO;
import com.avensys.rts.documentservice.payloadresponse.DocumentResponseDTO;

import java.util.List;

/**
 * @author Koh He Xiang This interface is used to define the methods for the
 *         Currency Service
 */
public interface DocumentService {

	/**
	 * This method is used to save document and create a document
	 * 
	 * @param documentRequest
	 * @return
	 */
	DocumentResponseDTO createDocument(DocumentRequestDTO documentRequest);

	/**
	 * This method is used to save a list of documents
	 * 
	 * @param documentRequestList
	 * @return DocumentResponseDTO
	 */
	List<DocumentResponseDTO> createDocumentList(List<DocumentRequestDTO> documentRequestList);

//    List<DocumentResponseDTO> updateDocumentList(List<DocumentRequestDTO> documentRequestList);
//
	/**
	 * This method is used to get document Entity Id and Entity Type
	 * 
	 * @param documentRequest
	 * @return DocumentResponseDTO
	 */
	DocumentResponseDTO updateDocumentByEntityIdAndEntityType(DocumentRequestDTO documentRequest);

	/**
	 * This method is used to get document id
	 * 
	 * @param documentRequest
	 * @return DocumentResponseDTO
	 */
	DocumentResponseDTO updateDocumentById(Integer documentId, DocumentRequestDTO documentRequest);

	/**
	 * This method is used to delete document by id
	 * 
	 * @param documentDeleteRequest
	 * @return void
	 */
	void deleteDocumentEntityIdAndType(DocumentDeleteRequestDTO documentDeleteRequest);

	/**
	 * This method is used to get document by entity type and entity id
	 * 
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	List<DocumentResponseDTO> getDocumentByEntityTypeAndEntityId(String entityType, Integer entityId);

	List<DocumentNewResponseDTO> getDocumentNewByEntityTypeAndEntityId(String entityType, Integer entityId);

	List<DocumentNewResponseDTO> getDocumentNewByUserEntityTypeAndEntityId(String entityType, Integer entityId,
			Long userId);

	/**
	 * This method is used to delete document by id
	 * 
	 * @param documentId
	 */
	void deleteDocumentById(Integer documentId);

	void deleteDocumentsByEntityTypeAndEntityId(String entityType, Integer entityId);

	DocumentDownloadResponseDTO downloadDocumentById(Integer documentId);

	DocumentDownloadResponseDTO downloadDocumentByEntity(String entityType, Integer entityId);

	DocumentDownloadResponseDTO downloadDocumentByEntityAndKey(String entityType, Integer entityId, String fileKey);

	void updateDocumentByKeysAndEntityTypeAndEntityId(UpdateDocumentListKeyDTO updateDocumentListKeyDTO);

}
