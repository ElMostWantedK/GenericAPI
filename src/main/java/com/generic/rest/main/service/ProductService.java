package com.generic.rest.main.service;

import com.generic.rest.main.dto.ProductDTO;
import com.generic.rest.main.dto.ProductImageDTO;
import com.generic.rest.main.model.Collection;
import com.generic.rest.main.model.Product;
import com.generic.rest.main.model.ProductImage;
import com.generic.rest.main.model.enums.ProductType;
import com.generic.rest.main.repository.CollectionRepository;
import com.generic.rest.main.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CollectionRepository collectionRepository;

    public ProductService(ProductRepository productRepository, CollectionRepository collectionRepository) {
        this.productRepository = productRepository;
        this.collectionRepository = collectionRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> listProducts(String name, ProductType type, Float minPrice, Float maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findActiveWithFilters(name, type, minPrice, maxPrice, pageable);

        return productPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDTO getProduct(Long id) {
        Product product = productRepository.findByIdActive(id)
                .orElseThrow(() -> new RuntimeException("Product not found or has been deleted"));
        return mapToResponse(product);
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO request) {
        validateOnlyOneMainImage(request.getImages());

        Product product = new Product(
                request.getName(),
                request.getDescription(),
                request.getNote(),
                request.getFabricDetails(),
                request.getType(),
                request.getGender(),
                request.getPrice(),
                request.getSizes()
        );

        for (ProductImageDTO imageDto : request.getImages()) {
            ProductImage image = new ProductImage(imageDto.getUrl(), imageDto.getIsMain(), product);
            product.addImage(image);
        }

        // Handle collections
        if (request.getCollectionIds() != null && !request.getCollectionIds().isEmpty()) {
            List<Collection> collections = collectionRepository.findAllById(request.getCollectionIds());
            if (collections.size() != request.getCollectionIds().size()) {
                throw new RuntimeException("One or more collections not found");
            }

            // Validate that collections don't have products in both parent and subcollection
            validateCollectionHierarchy(collections);

            for (Collection collection : collections) {
                collection.addProduct(product);
            }
        }

        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }

    @Transactional
    public ProductDTO editProduct(Long id, ProductDTO request) {
        Product product = productRepository.findByIdActive(id)
                .orElseThrow(() -> new RuntimeException("Product not found or has been deleted"));

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getNote() != null) {
            product.setNote(request.getNote());
        }
        if (request.getFabricDetails() != null) {
            product.setFabricDetails(request.getFabricDetails());
        }
        if (request.getType() != null) {
            product.setType(request.getType());
        }
        if (request.getGender() != null) {
            product.setGender(request.getGender());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getSizes() != null && !request.getSizes().isEmpty()) {
            product.setSizesList(request.getSizes());
        }
        if (request.getImages() != null) {
            validateOnlyOneMainImage(request.getImages());

            // Clear existing images and add new ones
            product.getImages().clear();
            for (ProductImageDTO imageDto : request.getImages()) {
                ProductImage image = new ProductImage(imageDto.getUrl(), imageDto.getIsMain(), product);
                product.addImage(image);
            }
        }

        // Handle collections update
        if (request.getCollectionIds() != null) {
            // Remove product from all current collections
            List<Collection> currentCollections = new ArrayList<>(product.getCollections());
            for (Collection collection : currentCollections) {
                collection.removeProduct(product);
            }
            product.getCollections().clear();

            // Add product to new collections
            if (!request.getCollectionIds().isEmpty()) {
                List<Collection> newCollections = collectionRepository.findAllById(request.getCollectionIds());
                if (newCollections.size() != request.getCollectionIds().size()) {
                    throw new RuntimeException("One or more collections not found");
                }

                // Validate that collections don't have products in both parent and subcollection
                validateCollectionHierarchy(newCollections);

                for (Collection collection : newCollections) {
                    collection.addProduct(product);
                }
            }
        }

        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findByIdActive(id)
                .orElseThrow(() -> new RuntimeException("Product not found or has been deleted"));
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    private ProductDTO mapToResponse(Product product) {
        List<ProductImageDTO> imageDtos = product.getImages().stream()
                .map(img -> new ProductImageDTO(img.getUrl(), img.getIsMain()))
                .collect(Collectors.toList());

        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getNote(),
                product.getFabricDetails(),
                product.getType(),
                product.getGender(),
                product.getPrice(),
                product.getSizesList(),
                imageDtos,
                product.getCollectionIds()
        );
    }

    private void validateOnlyOneMainImage(List<ProductImageDTO> images) {
        long mainImageCount = images.stream()
                .filter(ProductImageDTO::getIsMain)
                .count();

        if (mainImageCount != 1) {
            throw new RuntimeException("Exactly one image must be marked as main");
        }
    }

    private void validateCollectionHierarchy(List<Collection> collections) {
        // Check if any collection is a parent of another in the list
        for (Collection c1 : collections) {
            for (Collection c2 : collections) {
                if (!c1.equals(c2)) {
                    // Check if c1 is parent of c2
                    if (c2.hasParent() && c2.getParentCollection().equals(c1)) {
                        throw new RuntimeException("Cannot add product to both a collection and its subcollection");
                    }
                    // Check if c2 is parent of c1
                    if (c1.hasParent() && c1.getParentCollection().equals(c2)) {
                        throw new RuntimeException("Cannot add product to both a collection and its subcollection");
                    }
                }
            }
        }
    }
}
