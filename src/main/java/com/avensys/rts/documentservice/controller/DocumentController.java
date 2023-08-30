package com.avensys.rts.documentservice.controller;

import com.avensys.rts.documentservice.constant.MessageConstants;
import com.avensys.rts.documentservice.payload.DocumentDeleteRequestDTO;
import com.avensys.rts.documentservice.payload.DocumentRequestDTO;
import com.avensys.rts.documentservice.payload.DocumentResponseDTO;
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
import org.springframework.web.multipart.MultipartFile;

/***
 * @author Koh He Xiang
 * This class is used to define the endpoints for the Currency Controller
 */
@RestController
public class DocumentController {

    private final Logger log = LoggerFactory.getLogger(DocumentController.class);
    private final DocumentService documentService;
    private final MessageSource messageSource;

    public DocumentController(DocumentService documentService, MessageSource messageSource) {
        this.documentService = documentService;
        this.messageSource = messageSource;
    }

    /**
     * This method is used to get a currency by id
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
     * This method is used update document and document pdf in local
     * @param documentRequest
     * @return
     */
    @PutMapping(value = "/documents" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> updateDocument( @Valid @ModelAttribute DocumentRequestDTO documentRequest) {
        log.info("Document update: Controller");
        DocumentResponseDTO documentResponse = documentService.updateDocumentById(documentRequest);
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

}
