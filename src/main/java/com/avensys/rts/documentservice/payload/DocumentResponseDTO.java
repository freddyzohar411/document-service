package com.avensys.rts.documentservice.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * author: Koh He Xiang
 * This is the DTO class for the Currency response
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentResponseDTO {
    private int id;
    private String type;
    private String title;
    private String description;
    private int entityId;
}
