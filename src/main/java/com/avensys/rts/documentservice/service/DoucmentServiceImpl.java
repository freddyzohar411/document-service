package com.avensys.rts.documentservice.service;

import com.avensys.rts.documentservice.entity.DocumentEntity;
import com.avensys.rts.documentservice.payload.DocumentRequestDTO;
import com.avensys.rts.documentservice.payload.DocumentResponseDTO;
import com.avensys.rts.documentservice.repository.DocumentRepository;
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

    public DoucmentServiceImpl(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    /**
     * This method is used to save document and create a document
     * @param documentRequest
     * @return
     */
    @Override
    public DocumentResponseDTO createDocument(DocumentRequestDTO documentRequest) {
        DocumentEntity document = toDocumentEntity(documentRequest);
        System.out.println("Saving Doc");
        try {
            System.out.println("Current working directory: " + Paths.get("").toAbsolutePath().toString());
            // Create the directory if it doesn't exist
            Path directory = Paths.get("document-service/src/main/resources/uploaded/");
            if (!Files.exists(directory)) {
                System.out.println("Creating directory");
                Files.createDirectories(directory);
            }

            // Save the file
            byte[] bytes = documentRequest.getFile().getBytes();
            Path path = Paths.get("document-service/src/main/resources/uploaded/" + documentRequest.getFile().getOriginalFilename());
            Files.write(path, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DocumentEntity savedDocument = documentRepository.save(document);
        return toDocumentResponseDTO(savedDocument);
    }

    private DocumentEntity toDocumentEntity(DocumentRequestDTO documentRequest) {
        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setType(documentRequest.getType());
        documentEntity.setTitle(documentRequest.getTitle());
        documentEntity.setDescription(documentRequest.getDescription());
        documentEntity.setEntityId(documentRequest.getEntityId());
        return documentEntity;
    }

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
