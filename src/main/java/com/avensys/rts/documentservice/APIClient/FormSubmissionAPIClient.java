package com.avensys.rts.documentservice.APIClient;

import com.avensys.rts.documentservice.customresponse.HttpResponse;
import com.avensys.rts.documentservice.interceptor.JwtTokenInterceptor;
import com.avensys.rts.documentservice.payloadrequest.FormSubmissionsRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "form-service", url = "${api.form-submission.url}", configuration = JwtTokenInterceptor.class)
public interface FormSubmissionAPIClient {
    @PostMapping("/form-submissions")
    HttpResponse addFormSubmission(@RequestBody FormSubmissionsRequestDTO formSubmissionsRequestDTO);

    @GetMapping("/form-submissions/{formSubmissionId}")
    HttpResponse getFormSubmission(@PathVariable int formSubmissionId);

    @PutMapping("/form-submissions/{formSubmissionId}")
    HttpResponse updateFormSubmission(@PathVariable int formSubmissionId, @RequestBody FormSubmissionsRequestDTO formSubmissionsRequestDTO);

    @DeleteMapping("/form-submissions/{formSubmissionId}")
    HttpResponse deleteFormSubmission(@PathVariable int formSubmissionId);

}
