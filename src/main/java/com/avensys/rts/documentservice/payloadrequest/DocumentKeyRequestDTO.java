package com.avensys.rts.documentservice.payloadrequest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DocumentKeyRequestDTO {
	private String documentKey;
	private MultipartFile file;
}
