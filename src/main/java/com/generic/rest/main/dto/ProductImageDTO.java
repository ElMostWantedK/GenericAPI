package com.generic.rest.main.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProductImageDTO {

    @NotBlank(message = "Image URL is required")
    private String url;

    @NotNull(message = "isMain flag is required")
    private Boolean isMain;

    public ProductImageDTO() {
    }

    public ProductImageDTO(String url, Boolean isMain) {
        this.url = url;
        this.isMain = isMain;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getIsMain() {
        return isMain;
    }

    public void setIsMain(Boolean isMain) {
        this.isMain = isMain;
    }
}
