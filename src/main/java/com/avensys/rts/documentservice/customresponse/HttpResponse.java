package com.avensys.rts.documentservice.customresponse;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Koh He Xiang This class is used to create a custom response for the
 *         API calls. It is used to return a custom response to the client.
 */

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HttpResponse {
	private int code;
	private boolean error;
	private String message;
	private Object data;
	private Map<?, ?> audit;
	private LocalDateTime timestamp = LocalDateTime.now();
}
