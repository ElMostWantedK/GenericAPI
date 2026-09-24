package com.generic.rest.main.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SubcollectionDTO {

    private Long id;
    private String name;

    // Deserialize via setters: Jackson 3 would otherwise pick the all-args constructor
    @JsonCreator
    public SubcollectionDTO() {
    }

    public SubcollectionDTO(Long id, String name) {
        this.id = id;
        this.name = name;
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
}

