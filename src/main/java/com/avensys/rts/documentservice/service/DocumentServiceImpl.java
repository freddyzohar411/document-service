package com.avensys.rts.documentservice.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.avensys.rts.documentservice.APIClient.FormSubmissionAPIClient;
import com.avensys.rts.documentservice.APIClient.UserAPIClient;
import com.avensys.rts.documentservice.customresponse.HttpResponse;
import com.avensys.rts.documentservice.entity.DocumentEntity;
import com.avensys.rts.documentservice.payloadrequest.DocumentDeleteRequestDTO;
import com.avensys.rts.documentservice.payloadrequest.DocumentRequestDTO;
import com.avensys.rts.documentservice.payloadrequest.FormSubmissionsRequestDTO;
import com.avensys.rts.documentservice.payloadresponse.DocumentNewResponseDTO;
import com.avensys.rts.documentservice.payloadresponse.DocumentResponseDTO;
import com.avensys.rts.documentservice.payloadresponse.FormSubmissionsResponseDTO;
import com.avensys.rts.documentservice.payloadresponse.UserResponseDTO;
import com.avensys.rts.documentservice.repository.DocumentRepository;
import com.avensys.rts.documentservice.util.JwtUtil;
import com.avensys.rts.documentservice.util.MappingUtil;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

/**
 * @author Koh He Xiang This class is used to implement the methods for the
 *         Currency Service
 */
@Service
public class DocumentServiceImpl implements DocumentService {

	@Autowired
	private UserAPIClient userAPIClient;

	@Autowired
	private FormSubmissionAPIClient formSubmissionAPIClient;
	private final Logger log = LoggerFactory.getLogger(DocumentServiceImpl.class);
	private final DocumentRepository documentRepository;

	private final String UPLOAD_PATH = "document-service/src/main/resources/uploaded/";

	public DocumentServiceImpl(DocumentRepository documentRepository) {
		this.documentRepository = documentRepository;
	}

	/**
	 * This method is used to save document and create a document Account creation
	 * call this method
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
		document.setCreatedBy(documentRequest.getCreatedBy());
		document.setUpdatedBy(documentRequest.getUpdatedBy());
		DocumentEntity savedDocument = documentRepository.save(document);

		// Log the current working directory
		log.info("Current working directory: " + Paths.get("").toAbsolutePath().toString());

		// Create upload directory if it doesn't exist
		createDirectoryIfNotExist(UPLOAD_PATH);

		// Save pdf locally
		savePDFLocal(savedDocument, documentRequest);

		// Let send form data to form submission microservice
		if (documentRequest.getFormId() != null) {
			FormSubmissionsRequestDTO formSubmissionsRequestDTO = documentRequestDTOTFormSubmissionRequestDTO(
					savedDocument, documentRequest);
			HttpResponse formSubmissionResponse = formSubmissionAPIClient.addFormSubmission(formSubmissionsRequestDTO);
			FormSubmissionsResponseDTO formSubmissionData = MappingUtil
					.mapClientBodyToClass(formSubmissionResponse.getData(), FormSubmissionsResponseDTO.class);
			savedDocument.setFormSubmissionId(formSubmissionData.getId());
		}

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

	/**
	 * This method is used update document by Entity Id and Entity Type
	 *
	 * @param documentRequest
	 * @return
	 */
	@Override
	@Transactional
	public DocumentResponseDTO updateDocumentByEntityIdAndEntityType(DocumentRequestDTO documentRequest) {
		DocumentEntity documentFound = documentRepository
				.findOneByEntityTypeAndEntityId(documentRequest.getEntityType(), documentRequest.getEntityId())
				.orElseThrow(() -> new EntityNotFoundException("Document with type %s and entity id %s not found"
						.formatted(documentRequest.getType(), documentRequest.getEntityId())));

		// Delete pdf locally if it exist
		deletePDFLocal(documentFound);

		// Update document
		updateDocumentEntity(documentFound, documentRequest);

		// Update and save PDF locally
		savePDFLocal(documentFound, documentRequest);

		// Update document
		if (documentFound.getFormSubmissionId() != null) {
			FormSubmissionsRequestDTO formSubmissionsRequestDTO = documentRequestDTOTFormSubmissionRequestDTO(
					documentFound, documentRequest);
			HttpResponse formSubmissionResponse = formSubmissionAPIClient
					.updateFormSubmission(documentFound.getFormSubmissionId(), formSubmissionsRequestDTO);
			FormSubmissionsResponseDTO formSubmissionData = MappingUtil
					.mapClientBodyToClass(formSubmissionResponse.getData(), FormSubmissionsResponseDTO.class);
			documentFound.setFormSubmissionId(formSubmissionData.getId());
		}
		documentFound.setUpdatedBy(documentRequest.getUpdatedBy());
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
		DocumentEntity documentFound = documentRepository.findById(documentId)
				.orElseThrow(() -> new EntityNotFoundException("Document with id %s not found".formatted(documentId)));

		// Update file only if file in request is not null
		if (documentRequest.getFile() != null) {

			// Delete pdf locally if it exist
			deletePDFLocal(documentFound);

			// Update and save PDF locally
			savePDFLocal(documentFound, documentRequest);
		}
		// Update document
		updateDocumentEntity(documentFound, documentRequest);

		// Update formsubmission
		FormSubmissionsRequestDTO formSubmissionsRequestDTO = documentRequestDTOTFormSubmissionRequestDTO(documentFound,
				documentRequest);
		HttpResponse formSubmissionResponse = formSubmissionAPIClient
				.updateFormSubmission(documentFound.getFormSubmissionId(), formSubmissionsRequestDTO);
		FormSubmissionsResponseDTO formSubmissionData = MappingUtil
				.mapClientBodyToClass(formSubmissionResponse.getData(), FormSubmissionsResponseDTO.class);
		documentFound.setFormSubmissionId(formSubmissionData.getId());

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
		DocumentEntity documentFound = documentRepository
				.findByTypeAndEntityId(documentDeleteRequest.getType(), documentDeleteRequest.getEntityId())
				.orElseThrow(() -> new EntityNotFoundException("Document with type %s and entity id %s not found"
						.formatted(documentDeleteRequest.getType(), documentDeleteRequest.getEntityId())));

		deletePDFLocal(documentFound);

		// Delete form submission from form microservice
		if (documentFound.getFormSubmissionId() != null) {
			HttpResponse formSubmissionResponse = formSubmissionAPIClient
					.deleteFormSubmission(documentFound.getFormSubmissionId());
		}

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
	 * This method is used to get document by entity type and entity id (Dynamic
	 * form)
	 * 
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	@Override
	public List<DocumentNewResponseDTO> getDocumentNewByEntityTypeAndEntityId(String entityType, Integer entityId) {
		List<DocumentEntity> documentsFound = documentRepository.findByEntityTypeAndEntityId(entityType, entityId);
		return documentsFound.stream().map(this::documentEntityToDocumentNewResponseDTO).toList();
	}

	/**
	 * This method is used to delete document by id
	 *
	 * @param documentId
	 */
	@Override
	@Transactional
	public void deleteDocumentById(Integer documentId) {
		DocumentEntity documentFound = documentRepository.findById(documentId)
				.orElseThrow(() -> new EntityNotFoundException("Document with id %s not found".formatted(documentId)));

		deletePDFLocal(documentFound);

		// Delete form submission from form microservice
		if (documentFound.getFormSubmissionId() != null) {
			HttpResponse formSubmissionResponse = formSubmissionAPIClient
					.deleteFormSubmission(documentFound.getFormSubmissionId());
		}

		documentRepository.delete(documentFound);

		log.info("Document deleted : Service");
	}

	@Override
	@Transactional
	public void deleteDocumentsByEntityTypeAndEntityId(String entityType, Integer entityId) {
		List<DocumentEntity> documentEntityList = documentRepository.findByEntityTypeAndEntityId(entityType, entityId);
		if (!documentEntityList.isEmpty()) {
			documentEntityList.forEach(documentEntity -> {
				deletePDFLocal(documentEntity);
				// Delete form submission from form microservice
				if (documentEntity.getFormSubmissionId() != null) {
					HttpResponse formSubmissionResponse = formSubmissionAPIClient
							.deleteFormSubmission(documentEntity.getFormSubmissionId());
				}
				documentRepository.delete(documentEntity);
			});
		}
	}

	private DocumentNewResponseDTO documentEntityToDocumentNewResponseDTO(DocumentEntity documentEntity) {
		DocumentNewResponseDTO documentNewResponseDTO = new DocumentNewResponseDTO();
		documentNewResponseDTO.setId(documentEntity.getId());
		documentNewResponseDTO.setType(documentEntity.getType());
		documentNewResponseDTO.setTitle(documentEntity.getTitle());
		documentNewResponseDTO.setDescription(documentEntity.getDescription());
		documentNewResponseDTO.setFormId(documentEntity.getFormId());
		documentNewResponseDTO.setFormSubmissionId(documentEntity.getFormSubmissionId());

		// Get from data from form submission
		if (documentEntity.getFormSubmissionId() != null) {
			HttpResponse formSubmissionResponse = formSubmissionAPIClient
					.getFormSubmission(documentEntity.getFormSubmissionId());
			FormSubmissionsResponseDTO formSubmissionData = MappingUtil
					.mapClientBodyToClass(formSubmissionResponse.getData(), FormSubmissionsResponseDTO.class);
			documentNewResponseDTO
					.setSubmissionData(MappingUtil.convertJsonNodeToJSONString(formSubmissionData.getSubmissionData()));
		}
		return documentNewResponseDTO;
	}

	/**
	 * Internal Method to formsubmissionRequest
	 */
	private FormSubmissionsRequestDTO documentRequestDTOTFormSubmissionRequestDTO(DocumentEntity documentEntity,
			DocumentRequestDTO documentRequest) {
		FormSubmissionsRequestDTO formSubmissionsRequestDTO = new FormSubmissionsRequestDTO();
		formSubmissionsRequestDTO.setUserId(getUserId());
		formSubmissionsRequestDTO
				.setSubmissionData(MappingUtil.convertJSONStringToJsonNode(documentRequest.getFormData()));
		formSubmissionsRequestDTO.setFormId(documentRequest.getFormId());
		formSubmissionsRequestDTO.setEntityId(documentEntity.getId());
		formSubmissionsRequestDTO.setEntityType(documentRequest.getEntityType());
		return formSubmissionsRequestDTO;
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
		if (documentRequest.getFormId() != null) {
			documentEntity.setFormId(documentRequest.getFormId());
		}
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

	private Integer getUserId() {
		String email = JwtUtil.getEmailFromContext();
		HttpResponse userResponse = userAPIClient.getUserByEmail(email);
		UserResponseDTO userData = MappingUtil.mapClientBodyToClass(userResponse.getData(), UserResponseDTO.class);
		return userData.getId();
	}

	@Override
	public List<DocumentNewResponseDTO> getDocumentNewByUserEntityTypeAndEntityId(String entityType, Integer entityId,
			Long userId) {
		List<DocumentEntity> documentsFound = documentRepository.findByCreatedByAndEntityTypeAndEntityId(userId,
				entityType, entityId);
		return documentsFound.stream().map(this::documentEntityToDocumentNewResponseDTO).toList();
	}
}
