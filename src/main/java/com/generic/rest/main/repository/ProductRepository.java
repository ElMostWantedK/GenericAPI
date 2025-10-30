package com.generic.rest.main.repository;

import com.generic.rest.main.model.Product;
import com.generic.rest.main.model.enums.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL")
    Page<Product> findAllActive(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Product> findByIdActive(@Param("id") Long id);

    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL " +
           "AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:type IS NULL OR p.type = :type) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> findActiveWithFilters(@Param("name") String name,
                                        @Param("type") ProductType type,
                                        @Param("minPrice") Float minPrice,
                                        @Param("maxPrice") Float maxPrice,
                                        Pageable pageable);
}
