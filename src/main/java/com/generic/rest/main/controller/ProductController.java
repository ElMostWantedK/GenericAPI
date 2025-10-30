package com.generic.rest.main.controller;

import com.generic.rest.main.dto.ProductDTO;
import com.generic.rest.main.model.enums.ProductType;
import com.generic.rest.main.service.ProductService;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<ProductDTO>> listProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ProductType type,
            @RequestParam(required = false) Float minPrice,
            @RequestParam(required = false) Float maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<ProductDTO> response = productService.listProducts(name, type, minPrice, maxPrice, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/show/{id}")
    public ResponseEntity<ProductDTO> showProduct(@PathVariable Long id) {
        ProductDTO response = productService.getProduct(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO request) {
        ProductDTO response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<ProductDTO> editProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO request
    ) {
        ProductDTO response = productService.editProduct(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
