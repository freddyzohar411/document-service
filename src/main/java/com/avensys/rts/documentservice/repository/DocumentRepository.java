package com.avensys.rts.documentservice.repository;

import com.avensys.rts.documentservice.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * author: Koh He Xiang
 * This is the repository class for the currency table in the database
 */
public interface DocumentRepository extends JpaRepository<DocumentEntity, Integer> {

}
