package com.generic.rest.main.dto;

import com.generic.rest.main.model.enums.Gender;
import com.generic.rest.main.model.enums.ProductType;
import com.generic.rest.main.model.enums.Size;

import java.util.List;

public class ProductDTO {

    private Long id;
    private String name;
    private String description;
    private String note;
    private String fabricDetails;
    private ProductType type;
    private Gender gender;
    private Float price;
    private List<Size> sizes;
    private List<ProductImageDTO> images;
    private List<Long> collectionIds;

    public ProductDTO() {
    }

    public ProductDTO(Long id, String name, String description, String note, String fabricDetails,
                          ProductType type, Gender gender, Float price, List<Size> sizes, List<ProductImageDTO> images, List<Long> collectionIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.note = note;
        this.fabricDetails = fabricDetails;
        this.type = type;
        this.gender = gender;
        this.price = price;
        this.sizes = sizes;
        this.images = images;
        this.collectionIds = collectionIds;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getFabricDetails() {
        return fabricDetails;
    }

    public void setFabricDetails(String fabricDetails) {
        this.fabricDetails = fabricDetails;
    }

    public ProductType getType() {
        return type;
    }

    public void setType(ProductType type) {
        this.type = type;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public List<Size> getSizes() {
        return sizes;
    }

    public void setSizes(List<Size> sizes) {
        this.sizes = sizes;
    }

    public List<ProductImageDTO> getImages() {
        return images;
    }

    public void setImages(List<ProductImageDTO> images) {
        this.images = images;
    }

    public List<Long> getCollectionIds() {
        return collectionIds;
    }

    public void setCollectionIds(List<Long> collectionIds) {
        this.collectionIds = collectionIds;
    }
}
