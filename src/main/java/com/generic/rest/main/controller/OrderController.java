package com.generic.rest.main.controller;

import com.generic.rest.main.dto.OrderDTO;
import com.generic.rest.main.model.User;
import com.generic.rest.main.service.OrderService;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<OrderDTO> create(Authentication authentication, @Valid @RequestBody OrderDTO request) {
        User user = (User) authentication.getPrincipal();
        OrderDTO response = orderService.createOrder(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/list")
    public ResponseEntity<List<OrderDTO>> list(Authentication authentication, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        User user = (User) authentication.getPrincipal();
        List<OrderDTO> response = orderService.listOrders(user, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list/{userId}")
    public ResponseEntity<List<OrderDTO>> listByUserId(@PathVariable Long userId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        List<OrderDTO> response = orderService.listOrdersByUserId(userId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/show/{id}")
    public ResponseEntity<OrderDTO> show(Authentication authentication, @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        OrderDTO response = orderService.getOrder(user, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<OrderDTO> edit(Authentication authentication, @PathVariable Long id, @Valid @RequestBody OrderDTO request) {
        User user = (User) authentication.getPrincipal();
        OrderDTO response = orderService.editOrder(user, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        orderService.deleteOrder(user, id);
        return ResponseEntity.noContent().build();
    }
}
