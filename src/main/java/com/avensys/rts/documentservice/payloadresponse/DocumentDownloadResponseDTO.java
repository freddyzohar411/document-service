package com.avensys.rts.documentservice.payloadresponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DocumentDownloadResponseDTO {
	String fileName;
	String encodedFile;
}
