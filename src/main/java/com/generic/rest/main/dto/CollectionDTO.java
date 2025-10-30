package com.generic.rest.main.dto;

import java.util.List;

public class CollectionDTO {

    private Long id;
    private String name;
    private List<Long> productIds;
    private List<SubcollectionDTO> subcollections;
    private Long parentCollectionId;

    public CollectionDTO() {
    }

    public CollectionDTO(Long id, String name, List<Long> productIds,
                             List<SubcollectionDTO> subcollections, Long parentCollectionId) {
        this.id = id;
        this.name = name;
        this.productIds = productIds;
        this.subcollections = subcollections;
        this.parentCollectionId = parentCollectionId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }

    public List<SubcollectionDTO> getSubcollections() {
        return subcollections;
    }

    public void setSubcollections(List<SubcollectionDTO> subcollections) {
        this.subcollections = subcollections;
    }

    public Long getParentCollectionId() {
        return parentCollectionId;
    }

    public void setParentCollectionId(Long parentCollectionId) {
        this.parentCollectionId = parentCollectionId;
    }
}
