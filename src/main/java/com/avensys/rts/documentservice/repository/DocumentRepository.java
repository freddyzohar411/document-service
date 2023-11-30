package com.avensys.rts.documentservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avensys.rts.documentservice.entity.DocumentEntity;

/**
 * author: Koh He Xiang This is the repository class for the document table in
 * the database
 */
public interface DocumentRepository extends JpaRepository<DocumentEntity, Integer> {
	Optional<DocumentEntity> findByTypeAndEntityId(String name, int entityId);

	List<DocumentEntity> findByEntityTypeAndEntityId(String entityType, int entityId);

	List<DocumentEntity> findByCreatedByAndEntityTypeAndEntityId(Long createdBy, String entityType, int entityId);

	Optional<DocumentEntity> findOneByEntityTypeAndEntityId(String entityType, int entityId);
}
