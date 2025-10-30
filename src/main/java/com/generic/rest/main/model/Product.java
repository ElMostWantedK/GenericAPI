package com.generic.rest.main.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.generic.rest.main.model.enums.Gender;
import com.generic.rest.main.model.enums.ProductType;
import com.generic.rest.main.model.enums.Size;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column
    private String note;

    @Column
    private String fabricDetails;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private Float price;

    @Column(nullable = false)
    private String sizes; // Comma-separated Size enum values

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @ManyToMany(mappedBy = "products")
    private List<Collection> collections = new ArrayList<>();

    @Column(columnDefinition = "NUMERIC")
    private LocalDateTime deletedAt;

    public Product() {
    }

    public Product(String name, String description, String note, String fabricDetails,
                   ProductType type, Gender gender, Float price, List<Size> sizes) {
        this.name = name;
        this.description = description;
        this.note = note;
        this.fabricDetails = fabricDetails;
        this.type = type;
        this.gender = gender;
        this.price = price;
        this.setSizesList(sizes);
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

    public String getSizes() {
        return sizes;
    }

    public void setSizes(String sizes) {
        this.sizes = sizes;
    }

    public List<Size> getSizesList() {
        if (sizes == null || sizes.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(sizes.split(","))
                .map(String::trim)
                .map(Size::valueOf)
                .collect(Collectors.toList());
    }

    public void setSizesList(List<Size> sizesList) {
        if (sizesList == null || sizesList.isEmpty()) {
            this.sizes = "";
        } else {
            // Remove duplicates and join
            this.sizes = sizesList.stream()
                    .distinct()
                    .map(Size::name)
                    .collect(Collectors.joining(","));
        }
    }

    public List<ProductImage> getImages() {
        return images;
    }

    public void setImages(List<ProductImage> images) {
        this.images = images;
    }

    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    public void removeImage(ProductImage image) {
        images.remove(image);
        image.setProduct(null);
    }

    public List<Collection> getCollections() {
        return collections;
    }

    public void setCollections(List<Collection> collections) {
        this.collections = collections;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public List<Long> getCollectionIds() {
        return collections.stream()
                .map(Collection::getId)
                .collect(Collectors.toList());
    }
}
