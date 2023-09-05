package com.avensys.rts.documentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/***
 * @author Koh He Xiang
 * This is the entity class for the currency table in the database
 */
@Entity
@Table(name = "document")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentEntity {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "type", length = 10 )
    private String type;

    @Column(name = "title", length = 50 )
    private String title;

    @Column(name = "document_name", length = 50 )
    private String documentName;

    @Column(name = "description", length = 250 )
    private String description;

    @Column(name = "entity_id")
    private Integer entityId;

    @Column(name = "entity_type", length = 50 )
    private String entityType;

}
