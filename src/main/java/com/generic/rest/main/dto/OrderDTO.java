package com.generic.rest.main.dto;

import com.generic.rest.main.model.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public class OrderDTO {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private AddressDTO address;
    private List<OrderProductDTO> products;
    private Float total;
    private OrderStatus status;

    public OrderDTO() {
    }

    public OrderDTO(Long id, LocalDateTime createdAt, LocalDateTime shippedAt, LocalDateTime deliveredAt,
                        AddressDTO address, List<OrderProductDTO> products, Float total, OrderStatus status) {
        this.id = id;
        this.createdAt = createdAt;
        this.shippedAt = shippedAt;
        this.deliveredAt = deliveredAt;
        this.address = address;
        this.products = products;
        this.total = total;
        this.status = status;
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

    public AddressDTO getAddress() {
        return address;
    }

    public void setAddress(AddressDTO address) {
        this.address = address;
    }

    public List<OrderProductDTO> getProducts() {
        return products;
    }

    public void setProducts(List<OrderProductDTO> products) {
        this.products = products;
    }

    public Float getTotal() {
        return total;
    }

    public void setTotal(Float total) {
        this.total = total;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
