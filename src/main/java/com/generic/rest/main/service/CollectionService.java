package com.generic.rest.main.service;

import com.generic.rest.main.dto.CollectionDTO;
import com.generic.rest.main.dto.ProductDTO;
import com.generic.rest.main.dto.ProductImageDTO;
import com.generic.rest.main.dto.SubcollectionDTO;
import com.generic.rest.main.model.Collection;
import com.generic.rest.main.model.Product;
import com.generic.rest.main.repository.CollectionRepository;
import com.generic.rest.main.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final ProductRepository productRepository;

    public CollectionService(CollectionRepository collectionRepository, ProductRepository productRepository) {
        this.collectionRepository = collectionRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<CollectionDTO> listAllCollections() {
        return collectionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CollectionDTO> listRootCollections() {
        return collectionRepository.findAllRootCollections().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CollectionDTO getCollection(Long id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found"));
        return mapToResponse(collection);
    }

    @Transactional
    public CollectionDTO createCollection(CollectionDTO request) {
        Collection collection = new Collection(request.getName());

        // If parentCollectionId is provided, create as a subcollection
        if (request.getParentCollectionId() != null) {
            Collection parent = collectionRepository.findById(request.getParentCollectionId())
                    .orElseThrow(() -> new RuntimeException("Parent collection not found"));

            // Validate that parent is not itself a subcollection (max depth = 2)
            if (parent.hasParent()) {
                throw new RuntimeException("A subcollection cannot have its own subcollections");
            }

            // Remove all products from parent collection when adding a subcollection
            // Products should only exist in subcollections, not in parent collections
            List<Product> parentProducts = new java.util.ArrayList<>(parent.getProducts());
            for (Product product : parentProducts) {
                parent.removeProduct(product);
            }

            // Save the new collection first
            Collection savedCollection = collectionRepository.save(collection);

            // Add it as a subcollection to the parent
            parent.addSubcollection(savedCollection);
            collectionRepository.save(parent);

            return mapToResponse(savedCollection);
        }

        // If no parentId, create as a root collection
        Collection savedCollection = collectionRepository.save(collection);
        return mapToResponse(savedCollection);
    }

    @Transactional
    public CollectionDTO editCollection(Long id, CollectionDTO request) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        if (request.getName() != null) {
            collection.setName(request.getName());
        }

        // Handle parent collection changes
        if (request.getParentCollectionId() != null) {
            Long currentParentId = collection.hasParent() ? collection.getParentCollection().getId() : null;

            // If the parent is changing or being set for the first time
            if (!request.getParentCollectionId().equals(currentParentId)) {
                Collection newParent = collectionRepository.findById(request.getParentCollectionId())
                        .orElseThrow(() -> new RuntimeException("Parent collection not found"));

                // Validate that new parent is not itself a subcollection
                if (newParent.hasParent()) {
                    throw new RuntimeException("A subcollection cannot have its own subcollections");
                }

                // Validate that collection doesn't have subcollections (max depth = 2)
                if (collection.hasSubcollections()) {
                    throw new RuntimeException("Cannot set a collection with subcollections as a subcollection");
                }

                // Remove all products from new parent collection
                // Products should only exist in subcollections, not in parent collections
                List<Product> parentProducts = new java.util.ArrayList<>(newParent.getProducts());
                for (Product product : parentProducts) {
                    newParent.removeProduct(product);
                }

                // Remove from old parent if exists
                if (collection.hasParent()) {
                    Collection oldParent = collection.getParentCollection();
                    oldParent.removeSubcollection(collection);
                    collectionRepository.save(oldParent);
                }

                // Add to new parent
                newParent.addSubcollection(collection);
                collectionRepository.save(newParent);
            }
        }

        Collection updatedCollection = collectionRepository.save(collection);
        return mapToResponse(updatedCollection);
    }

    @Transactional
    public void deleteCollection(Long id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        // Remove collection from all products (unlink)
        for (Product product : collection.getProducts()) {
            product.getCollections().remove(collection);
        }
        collection.getProducts().clear();

        // Subcollection will be deleted by cascade
        collectionRepository.delete(collection);
    }

    @Transactional
    public void addProductToCollection(Long collectionId, Long productId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        Product product = productRepository.findByIdActive(productId)
                .orElseThrow(() -> new RuntimeException("Product not found or has been deleted"));

        // Validate that product is not in a subcollection of this collection
        if (collection.hasSubcollections()) {
            for (Collection subcollection : collection.getSubcollections()) {
                if (subcollection.getProducts().contains(product)) {
                    throw new RuntimeException("Product is already in a subcollection of this collection");
                }
            }
        }

        // Validate that product is not in parent collection if this is a subcollection
        if (collection.hasParent()) {
            Collection parent = collection.getParentCollection();
            if (parent.getProducts().contains(product)) {
                throw new RuntimeException("Product is already in the parent collection");
            }
        }

        collection.addProduct(product);
        collectionRepository.save(collection);
    }

    @Transactional
    public void removeProductFromCollection(Long collectionId, Long productId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        collection.removeProduct(product);
        collectionRepository.save(collection);
    }

    @Transactional
    public CollectionDTO addSubcollection(Long parentId, Long subcollectionId) {
        Collection parent = collectionRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent collection not found"));

        Collection subcollection = collectionRepository.findById(subcollectionId)
                .orElseThrow(() -> new RuntimeException("Subcollection not found"));

        // Validate that parent is not itself a subcollection
        if (parent.hasParent()) {
            throw new RuntimeException("A subcollection cannot have its own subcollections");
        }

        // Validate that subcollection doesn't have a parent already
        if (subcollection.hasParent()) {
            throw new RuntimeException("Subcollection already has a parent. Remove it first.");
        }

        // Validate that subcollection doesn't have subcollections (max depth = 2)
        if (subcollection.hasSubcollections()) {
            throw new RuntimeException("Cannot set a collection with subcollections as a subcollection");
        }

        // Validate that products don't conflict between parent and subcollection
        for (Product product : subcollection.getProducts()) {
            if (parent.getProducts().contains(product)) {
                throw new RuntimeException("Cannot add subcollection: product '" + product.getName() + "' is already in parent collection");
            }
        }

        // Remove all products from parent collection when adding a subcollection
        // Products should only exist in subcollections, not in parent collections
        List<Product> parentProducts = new java.util.ArrayList<>(parent.getProducts());
        for (Product product : parentProducts) {
            parent.removeProduct(product);
        }

        parent.addSubcollection(subcollection);
        Collection savedParent = collectionRepository.save(parent);
        return mapToResponse(savedParent);
    }

    @Transactional
    public void removeSubcollection(Long parentId, Long subcollectionId) {
        Collection parent = collectionRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent collection not found"));

        Collection subcollection = collectionRepository.findById(subcollectionId)
                .orElseThrow(() -> new RuntimeException("Subcollection not found"));

        if (!parent.hasSubcollections()) {
            throw new RuntimeException("Parent collection doesn't have subcollections");
        }

        if (!parent.getSubcollections().contains(subcollection)) {
            throw new RuntimeException("Subcollection does not belong to this parent collection");
        }

        parent.removeSubcollection(subcollection);
        collectionRepository.save(parent);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getCollectionProducts(Long collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        return collection.getProducts().stream()
                .map(this::mapProductToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CollectionDTO> getSubcollections(Long parentId) {
        Collection parent = collectionRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent collection not found"));

        if (!parent.hasSubcollections()) {
            throw new RuntimeException("Parent collection doesn't have subcollections");
        }

        return parent.getSubcollections().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CollectionDTO mapToResponse(Collection collection) {
        List<Long> productIds = collection.getProducts().stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        List<SubcollectionDTO> subcollections = collection.getSubcollections().stream()
                .map(sub -> new SubcollectionDTO(sub.getId(), sub.getName()))
                .collect(Collectors.toList());

        Long parentCollectionId = collection.hasParent() ? collection.getParentCollection().getId() : null;

        return new CollectionDTO(
                collection.getId(),
                collection.getName(),
                productIds,
                subcollections,
                parentCollectionId
        );
    }


    private ProductDTO mapProductToResponse(Product product) {
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
}
