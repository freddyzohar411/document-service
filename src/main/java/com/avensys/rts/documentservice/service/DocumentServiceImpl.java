package com.avensys.rts.documentservice.service;

import com.avensys.rts.documentservice.entity.DocumentEntity;
import com.avensys.rts.documentservice.payloadrequest.DocumentDeleteRequestDTO;
import com.avensys.rts.documentservice.payloadrequest.DocumentRequestDTO;
import com.avensys.rts.documentservice.payloadresponse.DocumentResponseDTO;
import com.avensys.rts.documentservice.repository.DocumentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Koh He Xiang
 * This class is used to implement the methods for the Currency Service
 */
@Service
public class DocumentServiceImpl implements DocumentService {

    private final Logger log = LoggerFactory.getLogger(DocumentServiceImpl.class);
    private final DocumentRepository documentRepository;

    private final String UPLOAD_PATH = "document-service/src/main/resources/uploaded/";

    public DocumentServiceImpl(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    /**
     * This method is used to save document and create a document
     * Account creation call this method
     *
     * @param documentRequest
     * @return DocumentResponseDTO
     */
    @Override
    @Transactional
    public DocumentResponseDTO createDocument(DocumentRequestDTO documentRequest) {
        // Check if file exist
        if (documentRequest.getFile() == null) {
            throw new EntityNotFoundException("File cannot be null");
        }

        DocumentEntity document = toDocumentEntity(documentRequest);
        DocumentEntity savedDocument = documentRepository.save(document);

        // Log the current working directory
        log.info("Current working directory: " + Paths.get("").toAbsolutePath().toString());

        // Create upload directory if it doesn't exist
        createDirectoryIfNotExist(UPLOAD_PATH);

        // Save pdf locally
        savePDFLocal(savedDocument, documentRequest);

        log.info("Document created and saved : Service");
        return toDocumentResponseDTO(savedDocument);
    }

    /**
     * This method is used to save a list of documents
     *
     * @param documentRequestList
     * @return
     */
    @Override
    public List<DocumentResponseDTO> createDocumentList(List<DocumentRequestDTO> documentRequestList) {
        List<DocumentResponseDTO> documentResponseDTOList = new ArrayList<>();
        documentRequestList.forEach(documentRequest -> {
            DocumentEntity document = toDocumentEntity(documentRequest);
            DocumentEntity savedDocument = documentRepository.save(document);

            // Log the current working directory
            log.info("Current working directory: " + Paths.get("").toAbsolutePath().toString());

            // Create the directory if it doesn't exist
            createDirectoryIfNotExist(UPLOAD_PATH);

            // Save pdf locally
            savePDFLocal(savedDocument, documentRequest);

            documentResponseDTOList.add(toDocumentResponseDTO(savedDocument));

            log.info("Document created and saved : Service");
        });
        return documentResponseDTOList;
    }

//    /**
//     * This method is used to update a list of documents
//     * @param documentRequestList
//     * @return
//     */
//    @Override
//    @Transactional
//    public List<DocumentResponseDTO> updateDocumentList(List<DocumentRequestDTO> documentRequestList) {
//        List<DocumentResponseDTO> documentResponseDTOList = new ArrayList<>();
//        documentRequestList.forEach(documentRequest -> {
//            // Check if document id exist else create a new document
//            if (documentRequest.getId() == null) {
//                DocumentEntity document = toDocumentEntity(documentRequest);
//                DocumentEntity savedDocument = documentRepository.save(document);
//
//                // Log the current working directory
//                log.info("Current working directory: " + Paths.get("").toAbsolutePath().toString());
//
//                // Create the directory if it doesn't exist
//                createDirectoryIfNotExist(UPLOAD_PATH);
//
//                // Save pdf locally
//                savePDFLocal(savedDocument, documentRequest);
//                documentResponseDTOList.add(toDocumentResponseDTO(savedDocument));
//                log.info("Document created and saved : Service");
//            } else {
//                DocumentEntity documentFound = documentRepository.findByEntityTypeAndEntityId(documentRequest.getEntityType(), documentRequest.getEntityId()).orElseThrow(
//                        () -> new EntityNotFoundException("Document with entity type %s and entity id %s not found".formatted(documentRequest.getEntityType(), documentRequest.getEntityId()))
//                );
//
//                // Delete pdf locally if it exist
//                deletePDFLocal(documentFound);
//
//                // Update document
//                updateDocumentEntity(documentFound, documentRequest);
//
//                // Update and save PDF locally
//                savePDFLocal(documentFound, documentRequest);
//                documentResponseDTOList.add(toDocumentResponseDTO(documentFound));
//                DocumentEntity updatedDocument = documentRepository.save(documentFound);
//                log.info("Document updated : Service");
//            }
//        });
//        return documentResponseDTOList;
//    }

//    /**
//     * This method is used to update document by id
//     * @param documentRequest
//     * @return DocumentResponseDTO
//     */

    /**
     * This method is used update document by Entity Id and Entity Type
     *
     * @param documentRequest
     * @return
     */
    @Override
    @Transactional
    public DocumentResponseDTO updateDocumentByEntityIdAndEntityType(DocumentRequestDTO documentRequest) {
        DocumentEntity documentFound = documentRepository.findOneByEntityTypeAndEntityId(documentRequest.getEntityType(), documentRequest.getEntityId()).orElseThrow(
                () -> new EntityNotFoundException("Document with type %s and entity id %s not found".formatted(documentRequest.getType(), documentRequest.getEntityId()))
        );

        // Delete pdf locally if it exist
        deletePDFLocal(documentFound);

        // Update document
        updateDocumentEntity(documentFound, documentRequest);

        // Update and save PDF locally
        savePDFLocal(documentFound, documentRequest);

        DocumentEntity updatedDocument = documentRepository.save(documentFound);
        log.info("Document updated : Service");
        return toDocumentResponseDTO(updatedDocument);
    }

    /**
     * This method is used to update document by id
     *
     * @param documentId
     * @param documentRequest
     * @return
     */
    @Override
    public DocumentResponseDTO updateDocumentById(Integer documentId, DocumentRequestDTO documentRequest) {
        DocumentEntity documentFound = documentRepository.findById(documentId).orElseThrow(
                () -> new EntityNotFoundException("Document with id %s not found".formatted(documentId))
        );

        // Update file only if file in request is not null
        if (documentRequest.getFile() != null) {

            // Delete pdf locally if it exist
            deletePDFLocal(documentFound);

            // Update and save PDF locally
            savePDFLocal(documentFound, documentRequest);
        }
        // Update document
        updateDocumentEntity(documentFound, documentRequest);
        DocumentEntity updatedDocument = documentRepository.save(documentFound);
        log.info("Document updated : Service");
        return toDocumentResponseDTO(updatedDocument);
    }

    /**
     * This method is used to delete document by entity id and type
     *
     * @param documentDeleteRequest
     */
    @Override
    public void deleteDocumentEntityIdAndType(DocumentDeleteRequestDTO documentDeleteRequest) {
        DocumentEntity documentFound = documentRepository.findByTypeAndEntityId(documentDeleteRequest.getType(), documentDeleteRequest.getEntityId()).orElseThrow(
                () -> new EntityNotFoundException("Document with type %s and entity id %s not found".formatted(documentDeleteRequest.getType(), documentDeleteRequest.getEntityId()))
        );

        deletePDFLocal(documentFound);
        documentRepository.delete(documentFound);

        log.info("Document deleted : Service");
    }

    /**
     * This method is used to get document by entity type and entity id
     *
     * @param entityType
     * @param entityId
     * @return List<DocumentResponseDTO>
     */
    @Override
    public List<DocumentResponseDTO> getDocumentByEntityTypeAndEntityId(String entityType, Integer entityId) {
        List<DocumentEntity> documentsFound = documentRepository.findByEntityTypeAndEntityId(entityType, entityId);
        return documentsFound.stream().map(this::toDocumentResponseDTO).toList();
    }

    /**
     * This method is used to delete document by id
     *
     * @param documentId
     */
    @Override
    public void deleteDocumentById(Integer documentId) {
        DocumentEntity documentFound = documentRepository.findById(documentId).orElseThrow(
                () -> new EntityNotFoundException("Document with id %s not found".formatted(documentId))
        );

        deletePDFLocal(documentFound);
        documentRepository.delete(documentFound);

        log.info("Document deleted : Service");
    }

    /**
     * Internal method used to create directory if it doesn't exist
     */
    private void createDirectoryIfNotExist(String path) {
        Path directory = Paths.get(path);
        if (!Files.exists(directory)) {
            System.out.println("Creating directory");
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Internal method used to save pdf file locally
     *
     * @param documentEntity
     * @param documentRequest
     */
    private void savePDFLocal(DocumentEntity documentEntity, DocumentRequestDTO documentRequest) {
        try {
            byte[] bytes = documentRequest.getFile().getBytes();
            Path path = Paths.get(UPLOAD_PATH + documentEntity.getId() + ".pdf");
            Files.write(path, bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Internal method used to delete pdf file locally
     *
     * @param documentFound
     */
    private void deletePDFLocal(DocumentEntity documentFound) {
        if (Files.exists(Paths.get(UPLOAD_PATH + documentFound.getId() + ".pdf"))) {
            try {
                Files.delete(Paths.get(UPLOAD_PATH + documentFound.getId() + ".pdf"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Internal method used to convert DocumentRequestDTO to DocumentEntity
     *
     * @param documentRequest
     * @return
     */
    private DocumentEntity toDocumentEntity(DocumentRequestDTO documentRequest) {
        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setType(documentRequest.getType());
        documentEntity.setTitle(documentRequest.getTitle());
        documentEntity.setDocumentName(documentRequest.getFile().getOriginalFilename());
        documentEntity.setDescription(documentRequest.getDescription());
        documentEntity.setEntityId(documentRequest.getEntityId());
        documentEntity.setEntityType(documentRequest.getEntityType());
        return documentEntity;
    }

    /**
     * Internal method used to convert DocumentEntity to DocumentResponseDTO
     *
     * @param documentEntity
     * @return
     */
    private DocumentResponseDTO toDocumentResponseDTO(DocumentEntity documentEntity) {
        DocumentResponseDTO documentResponseDTO = new DocumentResponseDTO();
        documentResponseDTO.setId(documentEntity.getId());
        documentResponseDTO.setType(documentEntity.getType());
        documentResponseDTO.setTitle(documentEntity.getTitle());
        documentResponseDTO.setDocumentName(documentEntity.getDocumentName());
        documentResponseDTO.setDescription(documentEntity.getDescription());
        documentResponseDTO.setEntityId(documentEntity.getEntityId());
        documentResponseDTO.setEntityType(documentEntity.getEntityType());
        return documentResponseDTO;
    }

    /**
     * Internal method used to update DocumentEntity
     *
     * @param documentEntity
     * @param documentRequest
     */
    private void updateDocumentEntity(DocumentEntity documentEntity, DocumentRequestDTO documentRequest) {
        documentEntity.setType(documentRequest.getType());
        documentEntity.setTitle(documentRequest.getTitle());
        documentEntity.setDescription(documentRequest.getDescription());
        if (documentRequest.getFile() != null) {
            documentEntity.setDocumentName(documentRequest.getFile().getOriginalFilename());
        }
        documentEntity.setEntityId(documentRequest.getEntityId());
        documentEntity.setEntityType(documentRequest.getEntityType());
    }
}
