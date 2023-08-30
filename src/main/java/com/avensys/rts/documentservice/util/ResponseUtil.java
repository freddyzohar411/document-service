package com.avensys.rts.documentservice.util;

import com.avensys.rts.documentservice.customresponse.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * This class is used to generate the response for the API calls.
 */
public class ResponseUtil {
    /**
     * This method is used to generate the success response.
     * @param dataObject
     * @param httpStatus
     * @param message
     * @return
     */
    public static ResponseEntity<Object> generateSuccessResponse(Object dataObject, HttpStatus httpStatus, String message) {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setData(dataObject);
        httpResponse.setCode(httpStatus.value());
        httpResponse.setMessage(message);
        return new ResponseEntity<>(httpResponse,httpStatus);
    }

    /**
     * This method is used to generate the error response.
     * @param httpStatus
     * @param message
     * @return
     */
    public static ResponseEntity<Object> generateErrorResponse(HttpStatus httpStatus, String message) {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setCode(httpStatus.value());
        httpResponse.setError(true);
        httpResponse.setMessage(message);

        return new ResponseEntity<>(httpResponse,httpStatus);
    }



}
