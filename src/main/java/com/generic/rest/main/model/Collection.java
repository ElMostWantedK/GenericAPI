package com.generic.rest.main.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "collections")
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToMany
    @JoinTable(
        name = "collection_products",
        joinColumns = @JoinColumn(name = "collection_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "parentCollection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Collection> subcollections = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "parent_collection_id")
    private Collection parentCollection;

    public Collection() {
    }

    public Collection(String name) {
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

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public void addProduct(Product product) {
        if (!this.products.contains(product)) {
            this.products.add(product);
            product.getCollections().add(this);
        }
    }

    public void removeProduct(Product product) {
        if (this.products.contains(product)) {
            this.products.remove(product);
            product.getCollections().remove(this);
        }
    }

    public List<Collection> getSubcollections() {
        return subcollections;
    }

    public void setSubcollections(List<Collection> subcollections) {
        this.subcollections = subcollections;
    }

    public void addSubcollection(Collection subcollection) {
        if (!this.subcollections.contains(subcollection)) {
            this.subcollections.add(subcollection);
            subcollection.setParentCollection(this);
        }
    }

    public void removeSubcollection(Collection subcollection) {
        if (this.subcollections.contains(subcollection)) {
            this.subcollections.remove(subcollection);
            subcollection.setParentCollection(null);
        }
    }

    public Collection getParentCollection() {
        return parentCollection;
    }

    public void setParentCollection(Collection parentCollection) {
        this.parentCollection = parentCollection;
    }

    public boolean hasParent() {
        return parentCollection != null;
    }

    public boolean hasSubcollections() {
        return subcollections != null && !subcollections.isEmpty();
    }

    public boolean isSubcollection() {
        return parentCollection != null;
    }
}
