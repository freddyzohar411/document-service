package com.avensys.rts.documentservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/***
 * @author Koh He Xiang This is the entity class for the currency table in the
 *         database
 */
@Entity
@Table(name = "document")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "type", length = 10)
	private String type;

	@Column(name = "title", length = 200)
	private String title;

	@Column(name = "document_name", length = 200)
	private String documentName;

	@Column(name = "description", length = 1000)
	private String description;

	@Column(name = "entity_id")
	private Integer entityId;

	@Column(name = "entity_type", length = 50)
	private String entityType;

	// Form entity
	@Column(name = "form_id", length = 50)
	private Integer formId;

	// Form submission entity
	@Column(name = "form_submission_id")
	private Integer formSubmissionId;

}
