package com.avensys.rts.documentservice.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.avensys.rts.documentservice.constant.MessageConstants;
import com.avensys.rts.documentservice.payloadrequest.DocumentDeleteRequestDTO;
import com.avensys.rts.documentservice.payloadrequest.DocumentListRequestDTO;
import com.avensys.rts.documentservice.payloadrequest.DocumentRequestDTO;
import com.avensys.rts.documentservice.payloadresponse.DocumentNewResponseDTO;
import com.avensys.rts.documentservice.payloadresponse.DocumentResponseDTO;
import com.avensys.rts.documentservice.service.DocumentService;
import com.avensys.rts.documentservice.util.JwtUtil;
import com.avensys.rts.documentservice.util.ResponseUtil;

import jakarta.validation.Valid;

/***
 * @author Koh He Xiang This class is used to define the endpoints for the
 *         Currency Controller
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/documents")
public class DocumentController {

	private final Logger log = LoggerFactory.getLogger(DocumentController.class);
	private final DocumentService documentService;
	private final MessageSource messageSource;

	@Autowired
	private JwtUtil jwtUtil;

	public DocumentController(DocumentService documentService, MessageSource messageSource) {
		this.documentService = documentService;
		this.messageSource = messageSource;
	}

	/**
	 * This method is used to create a document and document pdf in local
	 * 
	 * @param documentRequest
	 * @return HttpResponse with documentResponseDTO
	 */
	@PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Object> createDocument(@ModelAttribute DocumentRequestDTO documentRequest,
			@RequestHeader(name = "Authorization") String token) {
		log.info("Document create: Controller");
		Long userId = jwtUtil.getUserId(token);
		documentRequest.setCreatedBy(userId);
		documentRequest.setUpdatedBy(userId);
		DocumentResponseDTO documentResponse = documentService.createDocument(documentRequest);
		return ResponseUtil.generateSuccessResponse(documentResponse, HttpStatus.OK,
				messageSource.getMessage(MessageConstants.MESSAGE_SUCCESS, null, LocaleContextHolder.getLocale()));
	}

	/**
	 * This method is used to create a list of document and document pdf in local
	 * 
	 * @param documentRequestList
	 * @return
	 */
	@PostMapping(value = "/documentsList", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Object> createDocumentList(
			@Valid @ModelAttribute DocumentListRequestDTO documentRequestList) {
		System.out.println("documentRequestList: " + documentRequestList.getDocumentRequestList());
		log.info("Document create: Controller");
		List<DocumentResponseDTO> documentResponseList = documentService
				.createDocumentList(documentRequestList.getDocumentRequestList());
		return ResponseUtil.generateSuccessResponse(documentResponseList, HttpStatus.OK,
				messageSource.getMessage(MessageConstants.MESSAGE_SUCCESS, null, LocaleContextHolder.getLocale()));
	}

	/**
	 * This method is used update list of document and document pdf in local
	 * 
	 * @param documentRequestList
	 * @return
	 */
//    @PostMapping(value = "/documentsList/update" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<Object> updateDocumentList(@Valid @ModelAttribute DocumentListRequestDTO documentRequestList) {
//        System.out.println("documentRequestList: " + documentRequestList.getDocumentRequestList());
//        log.info("Document create: Controller");
//        List<DocumentResponseDTO> documentResponseList = documentService.updateDocumentList(documentRequestList.getDocumentRequestList());
//        return ResponseUtil.generateSuccessResponse(documentResponseList, HttpStatus.OK, messageSource.getMessage(MessageConstants.MESSAGE_SUCCESS, null, LocaleContextHolder.getLocale()));
//    }

	/**
	 * This method is used update document by Entity Id and Entity Type
	 * 
	 * @return
	 */
	@PutMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Object> updateDocument(@Valid @ModelAttribute DocumentRequestDTO documentRequest,
			@RequestHeader(name = "Authorization") String token) {
		log.info("Document update: Controller");
		Long userId = jwtUtil.getUserId(token);
		documentRequest.setUpdatedBy(userId);
		DocumentResponseDTO documentResponse = documentService.updateDocumentByEntityIdAndEntityType(documentRequest);
		return ResponseUtil.generateSuccessResponse(documentResponse, HttpStatus.OK,
				messageSource.getMessage(MessageConstants.MESSAGE_SUCCESS, null, LocaleContextHolder.getLocale()));
	}

	/**
	 * This method is used update document by document id
	 * 
	 * @return
	 */
	@PutMapping(value = "/{documentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Object> updateDocumentById(@PathVariable Integer documentId,
			@ModelAttribute DocumentRequestDTO documentRequest) {
		log.info("Document update: Controller");
		DocumentResponseDTO documentResponse = documentService.updateDocumentById(documentId, documentRequest);
		return ResponseUtil.generateSuccessResponse(documentResponse, HttpStatus.OK,
				messageSource.getMessage(MessageConstants.MESSAGE_SUCCESS, null, LocaleContextHolder.getLocale()));
	}

	/**
	 * This method is used to delete document by entity id and type
	 * 
	 * @param documentDeleteRequestDTO
	 * @return
	 */
	@DeleteMapping("")
	public ResponseEntity<Object> deleteDocumentByEntityIdAndType(
			@RequestBody DocumentDeleteRequestDTO documentDeleteRequestDTO) {
		log.info("Document delete: Controller");
		documentService.deleteDocumentEntityIdAndType(documentDeleteRequestDTO);
		return ResponseUtil.generateSuccessResponse(null, HttpStatus.OK,
				messageSource.getMessage(MessageConstants.MESSAGE_SUCCESS, null, LocaleContextHolder.getLocale()));
	}

	/**
	 * This method is used to delete document by document id
	 * 
	 * @param documentId
	 * @return
	 */
	@DeleteMapping("/{documentId}")
	public ResponseEntity<Object> deleteDocumentById(@PathVariable Integer documentId) {
		log.info("Document delete: Controller");
		documentService.deleteDocumentById(documentId);
		return ResponseUtil.generateSuccessResponse(null, HttpStatus.OK,
				messageSource.getMessage(MessageConstants.MESSAGE_SUCCESS, null, LocaleContextHolder.getLocale()));
	}

	/**
	 * This method is used to get document by entity type and entity id
	 * 
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	@GetMapping("")
	public ResponseEntity<Object> getDocumentByEntityTypeAndId(@RequestParam String entityType,
			@RequestParam int entityId) {
		log.info("Document get: Controller");
		List<DocumentResponseDTO> documentResponseDTO = documentService.getDocumentByEntityTypeAndEntityId(entityType,
				entityId);
		return ResponseUtil.generateSuccessResponse(documentResponseDTO, HttpStatus.OK,
				messageSource.getMessage(MessageConstants.MESSAGE_SUCCESS, null, LocaleContextHolder.getLocale()));
	}

	@GetMapping("/entity/{entityType}/{entityId}")
	public ResponseEntity<Object> getDocumentNewByEntityTypeAndId(@PathVariable String entityType,
			@PathVariable int entityId) {
		log.info("Document get: Controller");
		List<DocumentNewResponseDTO> documentResponseDTO = documentService
				.getDocumentNewByEntityTypeAndEntityId(entityType, entityId);
		return ResponseUtil.generateSuccessResponse(documentResponseDTO, HttpStatus.OK,
				messageSource.getMessage(MessageConstants.MESSAGE_SUCCESS, null, LocaleContextHolder.getLocale()));
	}

	@GetMapping("/entity/user/{entityType}/{entityId}")
	public ResponseEntity<Object> getDocumentNewByUserEntityTypeAndId(@PathVariable String entityType,
			@PathVariable int entityId, @RequestHeader(name = "Authorization") String token) {
		log.info("Document get: Controller");
		Long userId = jwtUtil.getUserId(token);
		List<DocumentNewResponseDTO> documentResponseDTO = documentService
				.getDocumentNewByUserEntityTypeAndEntityId(entityType, entityId, userId);
		return ResponseUtil.generateSuccessResponse(documentResponseDTO, HttpStatus.OK,
				messageSource.getMessage(MessageConstants.MESSAGE_SUCCESS, null, LocaleContextHolder.getLocale()));
	}

	@DeleteMapping("/entity/{entityType}/{entityId}")
	public ResponseEntity<Object> deleteDocumentsByEntityTypeAndEntityId(@PathVariable String entityType,
			@PathVariable Integer entityId) {
		log.info("Delete documents by entity type and entity id : Controller ");
		documentService.deleteDocumentsByEntityTypeAndEntityId(entityType, entityId);
		return ResponseUtil.generateSuccessResponse(null, HttpStatus.OK,
				messageSource.getMessage(MessageConstants.MESSAGE_SUCCESS, null, LocaleContextHolder.getLocale()));
	}

}
