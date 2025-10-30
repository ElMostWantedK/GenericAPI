package com.generic.rest.main.model;

import com.generic.rest.main.model.enums.OrderStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, columnDefinition = "NUMERIC")
    private LocalDateTime createdAt;

    @Column(name = "shipped_at", columnDefinition = "NUMERIC")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at", columnDefinition = "NUMERIC")
    private LocalDateTime deliveredAt;

    @ManyToOne
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> orderProducts = new ArrayList<>();

    public Order() {
    }

    public Order(LocalDateTime createdAt, Address address) {
        this.createdAt = createdAt;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<OrderProduct> getOrderProducts() {
        return orderProducts;
    }

    public void setOrderProducts(List<OrderProduct> orderProducts) {
        this.orderProducts = orderProducts;
    }

    public void addOrderProduct(OrderProduct orderProduct) {
        orderProducts.add(orderProduct);
        orderProduct.setOrder(this);
    }

    public void removeOrderProduct(OrderProduct orderProduct) {
        orderProducts.remove(orderProduct);
        orderProduct.setOrder(null);
    }

    public Float getTotal() {
        return orderProducts.stream()
                .map(op -> op.getPriceAtPurchase() * op.getQuantity())
                .reduce(0f, Float::sum);
    }

    public OrderStatus getStatus() {
        if (deliveredAt != null) {
            return OrderStatus.DELIVERED;
        } else if (shippedAt != null) {
            return OrderStatus.SHIPPED;
        } else {
            return OrderStatus.PENDING;
        }
    }
}
