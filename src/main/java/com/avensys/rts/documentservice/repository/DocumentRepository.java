package com.avensys.rts.documentservice.repository;

import com.avensys.rts.documentservice.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * author: Koh He Xiang
 * This is the repository class for the currency table in the database
 */
public interface DocumentRepository extends JpaRepository<DocumentEntity, Integer> {
    Optional<DocumentEntity> findByTypeAndEntityId(String name, int entityId);

}
