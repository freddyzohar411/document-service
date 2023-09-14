package com.avensys.rts.documentservice.controller;

import com.avensys.rts.documentservice.constant.MessageConstants;
import com.avensys.rts.documentservice.payloadrequest.DocumentDeleteRequestDTO;
import com.avensys.rts.documentservice.payloadrequest.DocumentListRequestDTO;
import com.avensys.rts.documentservice.payloadrequest.DocumentRequestDTO;
import com.avensys.rts.documentservice.payloadresponse.DocumentResponseDTO;
import com.avensys.rts.documentservice.service.DocumentService;
import com.avensys.rts.documentservice.util.ResponseUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/***
 * @author Koh He Xiang
 * This class is used to define the endpoints for the Currency Controller
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DocumentController {

    private final Logger log = LoggerFactory.getLogger(DocumentController.class);
    private final DocumentService documentService;
    private final MessageSource messageSource;

    public DocumentController(DocumentService documentService, MessageSource messageSource) {
        this.documentService = documentService;
        this.messageSource = messageSource;
    }

    /**
     * This method is used to create a document and document pdf in local
     * @param documentRequest
     * @return HttpResponse with documentResponseDTO
     */
    @PostMapping(value = "/documents" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> createDocument(@Valid @ModelAttribute DocumentRequestDTO documentRequest) {
        log.info("Document create: Controller");
        DocumentResponseDTO documentResponse = documentService.createDocument(documentRequest);
        return ResponseUtil.generateSuccessResponse(documentResponse, HttpStatus.OK, messageSource.getMessage(MessageConstants.MESSAGE_SUCCESS, null, LocaleContextHolder.getLocale()));
    }

    /**
     * This method is used to create a list of document and document pdf in local
     * @param documentRequestList
     * @return
     */
    @PostMapping(value = "/documentsList" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<Object> createDocumentList(@Valid @ModelAttribute DocumentListRequestDTO documentRequestList) {
        System.out.println("documentRequestList: " + documentRequestList.getDocumentRequestList());
        log.info("Document create: Controller");
        List<DocumentResponseDTO> documentResponseList = documentService.createDocumentList(documentRequestList.getDocumentRequestList());
        return ResponseUtil.generateSuccessResponse(documentResponseList, HttpStatus.OK, messageSource.getMessage(MessageConstants.MESSAGE_SUCCESS, null, LocaleContextHolder.getLocale()));
    }

    /**
     * This method is used update list of document and document pdf in local
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
     * @return
     */
    @PutMapping(value = "/documents" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> updateDocument( @Valid @ModelAttribute DocumentRequestDTO documentRequest) {
        log.info("Document update: Controller");
        DocumentResponseDTO documentResponse = documentService.updateDocumentByEntityIdAndEntityType(documentRequest);
        return ResponseUtil.generateSuccessResponse(documentResponse, HttpStatus.OK, messageSource.getMessage(MessageConstants.MESSAGE_SUCCESS, null, LocaleContextHolder.getLocale()));
    }

    /**
     * This method is used update document by document id
     * @return
     */
    @PutMapping(value = "/documents/{documentId}" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> updateDocumentById(@PathVariable Integer documentId,  @ModelAttribute DocumentRequestDTO documentRequest) {
        log.info("Document update: Controller");
        DocumentResponseDTO documentResponse = documentService.updateDocumentById(documentId, documentRequest);
        return ResponseUtil.generateSuccessResponse(documentResponse, HttpStatus.OK, messageSource.getMessage(MessageConstants.MESSAGE_SUCCESS, null, LocaleContextHolder.getLocale()));
    }

    /**
     * This method is used to delete document by entity id and type
     * @param documentDeleteRequestDTO
     * @return
     */
    @DeleteMapping("/documents")
    public ResponseEntity<Object> deleteDocumentByEntityIdAndType(@RequestBody DocumentDeleteRequestDTO documentDeleteRequestDTO) {
        log.info("Document delete: Controller");
        documentService.deleteDocumentEntityIdAndType(documentDeleteRequestDTO);
        return ResponseUtil.generateSuccessResponse(null, HttpStatus.OK, messageSource.getMessage(MessageConstants.MESSAGE_SUCCESS, null, LocaleContextHolder.getLocale()));
    }

    /**
     * This method is used to delete document by document id
     * @param documentId
     * @return
     */
    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<Object> deleteDocumentById(@PathVariable Integer documentId) {
        log.info("Document delete: Controller");
        documentService.deleteDocumentById(documentId);
        return ResponseUtil.generateSuccessResponse(null, HttpStatus.OK, messageSource.getMessage(MessageConstants.MESSAGE_SUCCESS, null, LocaleContextHolder.getLocale()));
    }

    /**
     * This method is used to get document by entity type and entity id
     * @param entityType
     * @param entityId
     * @return
     */
    @GetMapping("/documents")
    public ResponseEntity<Object> getDocumentByEntityTypeAndId(@RequestParam String entityType, @RequestParam int entityId) {
        log.info("Document get: Controller");
        List<DocumentResponseDTO> documentResponseDTO = documentService.getDocumentByEntityTypeAndEntityId(entityType, entityId);
        return ResponseUtil.generateSuccessResponse(documentResponseDTO, HttpStatus.OK, messageSource.getMessage(MessageConstants.MESSAGE_SUCCESS, null, LocaleContextHolder.getLocale()));
    }

}
