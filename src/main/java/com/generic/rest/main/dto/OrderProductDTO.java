package com.generic.rest.main.dto;

public class OrderProductDTO {

    private Long productId;
    private String productName;
    private Integer quantity;
    private Float priceAtPurchase;
    private Float subtotal;

    public OrderProductDTO() {
    }

    public OrderProductDTO(Long productId, String productName, Integer quantity, Float priceAtPurchase) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
        this.subtotal = priceAtPurchase * quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Float getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public void setPriceAtPurchase(Float priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
    }

    public Float getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Float subtotal) {
        this.subtotal = subtotal;
    }
}
