package com.generic.rest.main.repository;

import com.generic.rest.main.model.Order;
import com.generic.rest.main.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.address.user = :user")
    Page<Order> findByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.address.user.id = :userId")
    Page<Order> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
