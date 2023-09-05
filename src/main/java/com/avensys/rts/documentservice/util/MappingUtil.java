package com.avensys.rts.documentservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class MappingUtil {

    /**
     * This method is used to convert Object to Class
     * Use to convert API client Httpresponse back to DTO class
     *
     * @param body
     * @param mappedDTO <T>
     * @return T
     */
    public static <T> T mapClientBodyToClass(Object body, Class<T> mappedDTO) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.convertValue(body, mappedDTO);
    }
}
