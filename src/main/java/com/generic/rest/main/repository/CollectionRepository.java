package com.generic.rest.main.repository;

import com.generic.rest.main.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

    @Query("SELECT c FROM Collection c WHERE c.parentCollection IS NULL")
    List<Collection> findAllRootCollections();

    @Query("SELECT c FROM Collection c WHERE c.parentCollection.id = :parentId")
    List<Collection> findSubcollectionsByParentId(@Param("parentId") Long parentId);
}
