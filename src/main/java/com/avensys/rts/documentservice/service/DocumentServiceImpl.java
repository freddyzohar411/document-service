package com.avensys.rts.documentservice.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import com.avensys.rts.documentservice.payloadrequest.*;
import com.avensys.rts.documentservice.payloadresponse.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.avensys.rts.documentservice.APIClient.FormSubmissionAPIClient;
import com.avensys.rts.documentservice.APIClient.UserAPIClient;
import com.avensys.rts.documentservice.customresponse.HttpResponse;
import com.avensys.rts.documentservice.entity.DocumentEntity;
import com.avensys.rts.documentservice.repository.DocumentRepository;
import com.avensys.rts.documentservice.util.JwtUtil;
import com.avensys.rts.documentservice.util.MappingUtil;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

	@Value("${document.upload.path}")
	private String UPLOAD_PATH;

//	private final String UPLOAD_PATH = "document-service/src/main/resources/uploaded/";

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

	@Override
	public DocumentDownloadResponseDTO downloadDocumentById(Integer documentId) {
		DocumentEntity documentFound = documentRepository.findById(documentId)
				.orElseThrow(() -> new EntityNotFoundException("Document with id %s not found".formatted(documentId)));
//		// Get the file
//		Path path = Paths.get(UPLOAD_PATH + documentFound.getId() + ".pdf");
//		if (Files.exists(path)) {
//			//Convert the file to byte array
//			byte[] fileContent = null;
//			try {
//				fileContent = Files.readAllBytes(path);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			// Cnvert to encodeBase64String
//			String encodedString = java.util.Base64.getEncoder().encodeToString(fileContent);
//			DocumentDownloadResponseDTO documentDownloadResponseDTO = new DocumentDownloadResponseDTO();
//			documentDownloadResponseDTO.setEncodedFile(encodedString);
//			documentDownloadResponseDTO.setFileName(documentFound.getDocumentName());
//			return documentDownloadResponseDTO;
//		}
//		return null;

		return documentEntityToDocumentDownloadResponseDTO(documentFound);

	}

	@Override
	public DocumentDownloadResponseDTO downloadDocumentByEntity(String entityType, Integer entityId) {
		List<DocumentEntity> documentsFound = documentRepository.findByEntityTypeAndEntityId(entityType, entityId);
		System.out.println("Documents found: " + documentsFound);
		if (!documentsFound.isEmpty()) {
			DocumentEntity documentEntity = documentsFound.get(documentsFound.size() - 1);
			return documentEntityToDocumentDownloadResponseDTO(documentEntity);
		}
		return null;
	}

	@Override
	public DocumentDownloadResponseDTO downloadDocumentByEntityAndKey(String entityType, Integer entityId, String documentKey) {
		DocumentEntity documentEntity = documentRepository.findOneByEntityTypeAndEntityIdAndDocumentKey(entityType, entityId, documentKey).orElseThrow(
				() -> new EntityNotFoundException("Document with type %s, entity id %s and file key %s not found"
						.formatted(entityType, entityId, documentKey)));
		System.out.println("Document entity: " + documentEntity.getId());
		return documentEntityToDocumentDownloadResponseDTO(documentEntity);
	}

//	@Override
//	public void updateDocumentByKeysAndEntityTypeAndEntityId(UpdateDocumentListIKeyDTO updateDocumentListIKeyDTO) {
//		List<DocumentEntity> documentEntityList = documentRepository.findByEntityTypeAndEntityId(
//				updateDocumentListIKeyDTO.getEntityType(), updateDocumentListIKeyDTO.getEntityId());
//
//		if (!documentEntityList.isEmpty()) {
//			if (updateDocumentListIKeyDTO.getDocumentKeyRequestDTO() == null || updateDocumentListIKeyDTO.getDocumentKeyRequestDTO().length == 0) {
//				// Delete all documents
//				documentEntityList.forEach(documentEntity -> {
//					deletePDFLocal(documentEntity);
//					// Delete form submission from form microservice
//					if (documentEntity.getFormSubmissionId() != null) {
//						HttpResponse formSubmissionResponse = formSubmissionAPIClient
//								.deleteFormSubmission(documentEntity.getFormSubmissionId());
//					}
//					documentRepository.delete(documentEntity);
//				});
//			} else{
//				// Delete those documents that are not in the updateDocumentListIKeyDTO
//				documentEntityList.forEach(documentEntity -> {
//					boolean found = false;
//					for (int i = 0; i < updateDocumentListIKeyDTO.getDocumentKeyRequestDTO().length; i++) {
//						if (documentEntity.getDocumentKey().equals(updateDocumentListIKeyDTO.getDocumentKeyRequestDTO()[i].getDocumentKey())) {
//							found = true;
//							break;
//						}
//					}
//					if (!found) {
//						deletePDFLocal(documentEntity);
//						// Delete form submission from form microservice
//						if (documentEntity.getFormSubmissionId() != null) {
//							HttpResponse formSubmissionResponse = formSubmissionAPIClient
//									.deleteFormSubmission(documentEntity.getFormSubmissionId());
//						}
//						documentRepository.delete(documentEntity);
//					}
//				});
//
//				// After deleting, update the rest that exist or create new if not exist in db
//				for (int i = 0; i < updateDocumentListIKeyDTO.getDocumentKeyRequestDTO().length; i++) {
//					boolean found = false;
//					for (DocumentEntity documentEntity : documentEntityList) {
//						if (documentEntity.getDocumentKey().equals(updateDocumentListIKeyDTO.getDocumentKeyRequestDTO()[i].getDocumentKey())) {
//							found = true;
//							break;
//						}
//					}
//					if (!found) {
//						DocumentEntity documentEntity = new DocumentEntity();
//						documentEntity.setEntityType(updateDocumentListIKeyDTO.getEntityType());
//						documentEntity.setEntityId(updateDocumentListIKeyDTO.getEntityId());
//						documentEntity.setDocumentKey(updateDocumentListIKeyDTO.getDocumentKeyRequestDTO()[i].getDocumentKey());
//						documentEntity.setCreatedBy(getUserId().longValue());
//						documentEntity.setUpdatedBy(getUserId().longValue());
//						DocumentEntity savedDocument = documentRepository.save(documentEntity);
//					}
//
//					// If exist in both db and updateDocumentListIKeyDTO, update the document
//					if (found) {
//						DocumentEntity documentEntity = documentRepository.findByDocumentKeyAndEntityTypeAndEntityId(updateDocumentListIKeyDTO.getDocumentKeyRequestDTO()[i].getDocumentKey(), updateDocumentListIKeyDTO.getEntityType(), updateDocumentListIKeyDTO.getEntityId()).get();
//						documentEntity.setUpdatedBy(getUserId().longValue());
//						DocumentEntity savedDocument = documentRepository.save(documentEntity);
//					}
//				}
//			}
//		}
//	}

//	@Override
//	public void updateDocumentByKeysAndEntityTypeAndEntityId(UpdateDocumentListKeyDTO updateDocumentListKeyDTO) {
//		// Retrieve the existing documents from the database
//		List<DocumentEntity> documentEntityList = documentRepository.findByEntityTypeAndEntityId(
//				updateDocumentListKeyDTO.getEntityType(), updateDocumentListKeyDTO.getEntityId());
//
//		// If no existing documents are found, initialize the list
//		if (documentEntityList == null) {
//			documentEntityList = new ArrayList<>();
//		}
//
//		// If the request array is empty, delete all existing documents
//		if (updateDocumentListKeyDTO.getDocumentKeyRequestDTO() == null || updateDocumentListKeyDTO.getDocumentKeyRequestDTO().length == 0) {
//			for (DocumentEntity documentEntity : documentEntityList) {
//				deletePDFLocal(documentEntity);
//				if (documentEntity.getFormSubmissionId() != null) {
//					formSubmissionAPIClient.deleteFormSubmission(documentEntity.getFormSubmissionId());
//				}
//				documentRepository.delete(documentEntity);
//			}
//			return;
//		}
//
//		// Create a set of document keys from the request
//		DocumentKeyRequestDTO[] requestDocumentKeyDTOs = updateDocumentListKeyDTO.getDocumentKeyRequestDTO();
//		Set<String> requestDocumentKeys = Arrays.stream(requestDocumentKeyDTOs)
//				.map(DocumentKeyRequestDTO::getDocumentKey)
//				.collect(Collectors.toSet());
//
//		// Process existing documents: delete or update
//		Set<String> processedKeys = new HashSet<>();
//		for (DocumentEntity documentEntity : documentEntityList) {
//			boolean found = false;
//			for (DocumentKeyRequestDTO requestDocumentKeyDTO : requestDocumentKeyDTOs) {
//				if (documentEntity.getDocumentKey().equals(requestDocumentKeyDTO.getDocumentKey())) {
//					found = true;
//					processedKeys.add(requestDocumentKeyDTO.getDocumentKey());
//					documentEntity.setUpdatedBy(getUserId().longValue());
//					documentRepository.save(documentEntity);
//					break;
//				}
//			}
//			if (!found) {
//				deletePDFLocal(documentEntity);
//				if (documentEntity.getFormSubmissionId() != null) {
//					formSubmissionAPIClient.deleteFormSubmission(documentEntity.getFormSubmissionId());
//				}
//				documentRepository.delete(documentEntity);
//			}
//		}
//
//		// Create new documents for request keys not already processed
//		for (DocumentKeyRequestDTO requestDocumentKeyDTO : requestDocumentKeyDTOs) {
//			if (!processedKeys.contains(requestDocumentKeyDTO.getDocumentKey())) {
//				DocumentEntity newDocumentEntity = new DocumentEntity();
//				newDocumentEntity.setEntityType(updateDocumentListKeyDTO.getEntityType());
//				newDocumentEntity.setEntityId(updateDocumentListKeyDTO.getEntityId());
//				newDocumentEntity.setDocumentKey(requestDocumentKeyDTO.getDocumentKey());
//				newDocumentEntity.setCreatedBy(getUserId().longValue());
//				newDocumentEntity.setUpdatedBy(getUserId().longValue());
//				documentRepository.save(newDocumentEntity);
//			}
//		}
//	}

	@Override
	public void updateDocumentByKeysAndEntityTypeAndEntityId(UpdateDocumentListKeyDTO updateDocumentListKeyDTO) {
		// Retrieve the existing documents from the database
		List<DocumentEntity> documentEntityList = documentRepository.findByEntityTypeAndEntityId(
				updateDocumentListKeyDTO.getEntityType(), updateDocumentListKeyDTO.getEntityId());

		// If no existing documents are found, initialize the list
		if (documentEntityList == null) {
			documentEntityList = new ArrayList<>();
		}

		// If the request array is empty, delete all existing documents
		if (updateDocumentListKeyDTO.getFileKeys() == null || updateDocumentListKeyDTO.getFileKeys().length == 0) {
			for (DocumentEntity documentEntity : documentEntityList) {
				deletePDFLocal(documentEntity);
				if (documentEntity.getFormSubmissionId() != null) {
					formSubmissionAPIClient.deleteFormSubmission(documentEntity.getFormSubmissionId());
				}
				documentRepository.delete(documentEntity);
			}
			return;
		}

		// Create a set of file keys from the request
		String[] requestFileKeys = updateDocumentListKeyDTO.getFileKeys();
		Set<String> requestFileKeySet = new HashSet<>(Arrays.asList(requestFileKeys));

		// Process existing documents: delete or update
		Set<String> processedKeys = new HashSet<>();
		for (DocumentEntity documentEntity : documentEntityList) {
			boolean found = false;
			for (String requestFileKey : requestFileKeys) {
				if (documentEntity.getDocumentKey().equals(requestFileKey)) {
					found = true;
					processedKeys.add(requestFileKey);
					documentEntity.setUpdatedBy(getUserId().longValue());
					// If there is a file, update the document
					if (updateDocumentListKeyDTO.getFiles() != null) {
						MultipartFile file = null;
						for (int i = 0; i < requestFileKeys.length; i++) {
							if (requestFileKeys[i].equals(requestFileKey)) {
								file = updateDocumentListKeyDTO.getFiles()[i];
								break;
							}
						}
						// Check if file is not a mockmultipart file
						if (file != null) {
							// Check if file name is not equals to mock_emptyFile
							System.out.println("File name: " + file.getOriginalFilename());
							if (!file.getOriginalFilename().isEmpty()) {
								System.out.println("File is not mock");
								// Update the file using existing methods (update)
								DocumentRequestDTO documentRequest = new DocumentRequestDTO();
								documentRequest.setEntityId(updateDocumentListKeyDTO.getEntityId());
								documentRequest.setEntityType(updateDocumentListKeyDTO.getEntityType());
								documentRequest.setDocumentKey(requestFileKey);
								documentRequest.setFile(file);
								documentRequest.setCreatedBy(getUserId().longValue());
								documentRequest.setUpdatedBy(getUserId().longValue());
								updateDocumentEntity(documentEntity, documentRequest);
								savePDFLocal(documentEntity, documentRequest);
							}
						}
					}
					documentRepository.save(documentEntity);
					break;
				}
			}
			if (!found) {
				deletePDFLocal(documentEntity);
				if (documentEntity.getFormSubmissionId() != null) {
					formSubmissionAPIClient.deleteFormSubmission(documentEntity.getFormSubmissionId());
				}
				documentRepository.delete(documentEntity);
			}
		}

		// Handle the files and create new documents for request keys not already
		// processed
		MultipartFile[] files = updateDocumentListKeyDTO.getFiles();
		if (files != null) {
			for (int i = 0; i < requestFileKeys.length; i++) {
				String requestFileKey = requestFileKeys[i];
				if (!processedKeys.contains(requestFileKey)) {
					if (files[i] != null) {
						System.out.println("WHY AM I HERE");
						MultipartFile file = files[i];
						DocumentRequestDTO documentRequest = new DocumentRequestDTO();
						documentRequest.setEntityType(updateDocumentListKeyDTO.getEntityType());
						documentRequest.setEntityId(updateDocumentListKeyDTO.getEntityId());
						documentRequest.setDocumentKey(requestFileKey);
						documentRequest.setFile(file);
						documentRequest.setCreatedBy(getUserId().longValue());
						documentRequest.setUpdatedBy(getUserId().longValue());

						DocumentResponseDTO documentResponse = createDocument(documentRequest);
						processedKeys.add(requestFileKey);
					}
				}
			}
		}
	}

	private DocumentDownloadResponseDTO documentEntityToDocumentDownloadResponseDTO(DocumentEntity documentEntity) {
		// Get the file
		Path path = Paths.get(UPLOAD_PATH + documentEntity.getId() + ".pdf");
		if (Files.exists(path)) {
			// Convert the file to byte array
			byte[] fileContent = null;
			try {
				fileContent = Files.readAllBytes(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Cnvert to encodeBase64String
			String encodedString = java.util.Base64.getEncoder().encodeToString(fileContent);
			DocumentDownloadResponseDTO documentDownloadResponseDTO = new DocumentDownloadResponseDTO();
			documentDownloadResponseDTO.setEncodedFile(encodedString);
			documentDownloadResponseDTO.setFileName(documentEntity.getDocumentName());
			return documentDownloadResponseDTO;
		}
		return null;
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
		if (documentRequest.getDocumentKey() != null) {
			documentEntity.setDocumentKey(documentRequest.getDocumentKey());
		}
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
		documentResponseDTO.setDocumentKey(documentEntity.getDocumentKey());
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
		documentEntity.setDocumentName(documentRequest.getFile().getOriginalFilename());
		if (documentRequest.getFile() != null) {
			documentEntity.setDocumentName(documentRequest.getFile().getOriginalFilename());
		}
		documentEntity.setEntityId(documentRequest.getEntityId());
		documentEntity.setEntityType(documentRequest.getEntityType());
		if (documentRequest.getDocumentKey() != null) {
			documentEntity.setDocumentKey(documentRequest.getDocumentKey());
		}
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
