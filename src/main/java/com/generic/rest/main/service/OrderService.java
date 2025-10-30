package com.generic.rest.main.service;

import com.generic.rest.main.dto.AddressDTO;
import com.generic.rest.main.dto.OrderDTO;
import com.generic.rest.main.dto.OrderProductDTO;
import com.generic.rest.main.model.*;
import com.generic.rest.main.repository.AddressRepository;
import com.generic.rest.main.repository.OrderRepository;
import com.generic.rest.main.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, AddressRepository addressRepository,
                       ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderDTO createOrder(User user, OrderDTO request) {
        // Validate address belongs to user
        Address address = addressRepository.findById(request.getAddress().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Address does not belong to user");
        }

        // Create order
        Order order = new Order(LocalDateTime.now(), address);

        // Add products to order
        for (OrderProductDTO productDto : request.getProducts()) {
            Product product = productRepository.findById(productDto.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Product not found: " + productDto.getProductId()));

            OrderProduct orderProduct = new OrderProduct(order, product, productDto.getQuantity(), product.getPrice());
            order.addOrderProduct(orderProduct);
        }

        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> listOrders(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderRepository.findByUser(user, pageable);

        List<OrderDTO> orderResponses = new ArrayList<>();
        for (Order order : orderPage.getContent()) {
            orderResponses.add(mapToResponse(order));
        }

        return orderResponses;
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> listOrdersByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderRepository.findByUserId(userId, pageable);

        List<OrderDTO> orderResponses = new ArrayList<>();
        for (Order order : orderPage.getContent()) {
            orderResponses.add(mapToResponse(order));
        }

        return orderResponses;
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrder(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        // Verify order belongs to user
        if (!order.getAddress().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Order does not belong to user");
        }

        return mapToResponse(order);
    }

    @Transactional
    public OrderDTO editOrder(User user, Long orderId, OrderDTO request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        // Verify order belongs to user
        if (!order.getAddress().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Order does not belong to user");
        }

        // Update order dates
        if (request.getShippedAt() != null) {
            order.setShippedAt(request.getShippedAt());
        }
        if (request.getDeliveredAt() != null) {
            order.setDeliveredAt(request.getDeliveredAt());
        }

        // Note: status is derived from shippedAt and deliveredAt, so no need to set it explicitly

        Order updatedOrder = orderRepository.save(order);
        return mapToResponse(updatedOrder);
    }

    @Transactional
    public void deleteOrder(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        // Verify order belongs to user
        if (!order.getAddress().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Order does not belong to user");
        }

        orderRepository.delete(order);
    }

    private OrderDTO mapToResponse(Order order) {
        // Map address
        Address addr = order.getAddress();
        AddressDTO addressResponse = new AddressDTO(
                addr.getId(),
                addr.getFirstName(),
                addr.getLastName(),
                addr.getAddress(),
                addr.getAddress2(),
                addr.getArea(),
                addr.getState(),
                addr.getCountry(),
                addr.getZipCode(),
                addr.getPhoneNumber(),
                addr.isDefault()
        );

        // Map order products
        List<OrderProductDTO> productResponses = new ArrayList<>();
        for (OrderProduct op : order.getOrderProducts()) {
            productResponses.add(new OrderProductDTO(
                    op.getProduct().getId(),
                    op.getProduct().getName(),
                    op.getQuantity(),
                    op.getPriceAtPurchase()
            ));
        }

        return new OrderDTO(
                order.getId(),
                order.getCreatedAt(),
                order.getShippedAt(),
                order.getDeliveredAt(),
                addressResponse,
                productResponses,
                order.getTotal(),
                order.getStatus()
        );
    }
}
