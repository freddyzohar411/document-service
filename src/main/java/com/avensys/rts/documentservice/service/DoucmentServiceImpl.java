package com.avensys.rts.documentservice.service;

import com.avensys.rts.documentservice.entity.DocumentEntity;
import com.avensys.rts.documentservice.payload.DocumentDeleteRequestDTO;
import com.avensys.rts.documentservice.payload.DocumentRequestDTO;
import com.avensys.rts.documentservice.payload.DocumentResponseDTO;
import com.avensys.rts.documentservice.repository.DocumentRepository;
import com.avensys.rts.documentservice.util.FileUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Koh He Xiang
 * This class is used to implement the methods for the Currency Service
 */
@Service
public class DoucmentServiceImpl implements DocumentService {

    private final Logger log = LoggerFactory.getLogger(DoucmentServiceImpl.class);
    private final DocumentRepository documentRepository;

    private final String UPLOAD_PATH = "document-service/src/main/resources/uploaded/";

    public DoucmentServiceImpl(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    /**
     * This method is used to save document and create a document
     *
     * @param documentRequest
     * @return DocumentResponseDTO
     */
    @Override
    public DocumentResponseDTO createDocument(DocumentRequestDTO documentRequest) {
        DocumentEntity document = toDocumentEntity(documentRequest);
        DocumentEntity savedDocument = documentRepository.save(document);
        try {
            System.out.println("Current working directory: " + Paths.get("").toAbsolutePath().toString());
            // Create the directory if it doesn't exist
            Path directory = Paths.get(UPLOAD_PATH);
            if (!Files.exists(directory)) {
                System.out.println("Creating directory");
                Files.createDirectories(directory);
            }

            // Save pdf locally
            savePDFLocal(savedDocument, documentRequest);

        } catch (IOException e) {
            e.printStackTrace();
        }

        log.info("Document created and saved : Service");
        return toDocumentResponseDTO(savedDocument);
    }

    /**
     * This method is used to update document by id
     *
     * @param documentRequest
     * @return DocumentResponseDTO
     */
    @Override
    @Transactional
    public DocumentResponseDTO updateDocumentById(DocumentRequestDTO documentRequest) {
        DocumentEntity documentFound = documentRepository.findByTypeAndEntityId(documentRequest.getType(), documentRequest.getEntityId()).orElseThrow(
                () -> new EntityNotFoundException("Document with type %s and entity id %s not found".formatted(documentRequest.getType(), documentRequest.getEntityId()))
        );

        // Delete pdf locally if it exist
        deletePDFLocal(documentFound);

        // Update document
        documentFound.setType(documentRequest.getType());
        documentFound.setTitle(documentRequest.getTitle());
        documentFound.setDescription(documentRequest.getDescription());
        documentFound.setEntityId(documentRequest.getEntityId());

        // Update and save PDF locally
        savePDFLocal(documentFound, documentRequest);

        DocumentEntity updatedDocument = documentRepository.save(documentFound);
        log.info("Document updated : Service");
        return toDocumentResponseDTO(updatedDocument);
    }

    /**
     * This method is used to delete document by entity id and type
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
     * Internal method used to save pdf file locally
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
     * @param documentRequest
     * @return
     */
    private DocumentEntity toDocumentEntity(DocumentRequestDTO documentRequest) {
        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setType(documentRequest.getType());
        documentEntity.setTitle(documentRequest.getTitle());
        documentEntity.setDescription(documentRequest.getDescription());
        documentEntity.setEntityId(documentRequest.getEntityId());
        return documentEntity;
    }

    /**
     * Internal method used to convert DocumentEntity to DocumentResponseDTO
     * @param documentEntity
     * @return
     */
    private DocumentResponseDTO toDocumentResponseDTO(DocumentEntity documentEntity) {
        DocumentResponseDTO documentResponseDTO = new DocumentResponseDTO();
        documentResponseDTO.setId(documentEntity.getId());
        documentResponseDTO.setType(documentEntity.getType());
        documentResponseDTO.setTitle(documentEntity.getTitle());
        documentResponseDTO.setDescription(documentEntity.getDescription());
        documentResponseDTO.setEntityId(documentEntity.getEntityId());
        return documentResponseDTO;
    }
}
