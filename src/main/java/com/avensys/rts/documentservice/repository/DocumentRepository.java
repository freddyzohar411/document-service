package com.avensys.rts.documentservice.repository;

import com.avensys.rts.documentservice.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.print.Doc;
import java.util.List;
import java.util.Optional;

/**
 * author: Koh He Xiang
 * This is the repository class for the document table in the database
 */
public interface DocumentRepository extends JpaRepository<DocumentEntity, Integer> {
    Optional<DocumentEntity> findByTypeAndEntityId(String name, int entityId);
    List<DocumentEntity> findByEntityTypeAndEntityId(String entityType, int entityId);

    Optional<DocumentEntity> findOneByEntityTypeAndEntityId(String entityType, int entityId);
}
