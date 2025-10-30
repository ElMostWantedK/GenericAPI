package com.generic.rest.main.controller;

import com.generic.rest.main.dto.CollectionDTO;
import com.generic.rest.main.dto.ProductDTO;
import com.generic.rest.main.service.CollectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/collection")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<CollectionDTO>> listCollections(
            @RequestParam(required = false, defaultValue = "false") boolean onlyRoot
    ) {
        List<CollectionDTO> response = onlyRoot
                ? collectionService.listRootCollections()
                : collectionService.listAllCollections();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/show/{id}")
    public ResponseEntity<CollectionDTO> showCollection(@PathVariable Long id) {
        CollectionDTO response = collectionService.getCollection(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<CollectionDTO> createCollection(@Valid @RequestBody CollectionDTO request) {
        CollectionDTO response = collectionService.createCollection(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<CollectionDTO> editCollection(
            @PathVariable Long id,
            @Valid @RequestBody CollectionDTO request
    ) {
        CollectionDTO response = collectionService.editCollection(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable Long id) {
        collectionService.deleteCollection(id);
        return ResponseEntity.noContent().build();
    }

    // @PostMapping("/{collectionId}/products/{productId}")
    // public ResponseEntity<Void> addProductToCollection(
    //         @PathVariable Long collectionId,
    //         @PathVariable Long productId
    // ) {
    //     collectionService.addProductToCollection(collectionId, productId);
    //     return ResponseEntity.ok().build();
    // }

    // @DeleteMapping("/{collectionId}/products/{productId}")
    // public ResponseEntity<Void> removeProductFromCollection(
    //         @PathVariable Long collectionId,
    //         @PathVariable Long productId
    // ) {
    //     collectionService.removeProductFromCollection(collectionId, productId);
    //     return ResponseEntity.noContent().build();
    // }

    @GetMapping("/{collectionId}/products")
    public ResponseEntity<List<ProductDTO>> getCollectionProducts(@PathVariable Long collectionId) {
        List<ProductDTO> response = collectionService.getCollectionProducts(collectionId);
        return ResponseEntity.ok(response);
    }

    // @PostMapping("/{parentId}/subcollections/{subcollectionId}")
    // public ResponseEntity<CollectionDTO> addSubcollection(
    //         @PathVariable Long parentId,
    //         @PathVariable Long subcollectionId
    // ) {
    //     CollectionDTO response = collectionService.addSubcollection(parentId, subcollectionId);
    //     return ResponseEntity.ok(response);
    // }

    // @DeleteMapping("/{parentId}/subcollections/{subcollectionId}")
    // public ResponseEntity<Void> removeSubcollection(
    //         @PathVariable Long parentId,
    //         @PathVariable Long subcollectionId
    // ) {
    //     collectionService.removeSubcollection(parentId, subcollectionId);
    //     return ResponseEntity.noContent().build();
    // }

    @GetMapping("/{parentId}/subcollections")
    public ResponseEntity<List<CollectionDTO>> getSubcollections(@PathVariable Long parentId) {
        List<CollectionDTO> response = collectionService.getSubcollections(parentId);
        return ResponseEntity.ok(response);
    }
}
