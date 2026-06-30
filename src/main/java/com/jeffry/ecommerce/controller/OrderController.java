package com.jeffry.ecommerce.controller;

import com.jeffry.ecommerce.dto.OrderResponse;
import com.jeffry.ecommerce.dto.OrderStatusUpdateRequest;
import com.jeffry.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(Authentication authentication) {
        return ResponseEntity.ok(orderService.createFromCart(authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> findMyOrders(Authentication authentication) {
        return ResponseEntity.ok(orderService.findByUser(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(Authentication authentication, @PathVariable Long id) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(orderService.findById(id, authentication.getName(), isAdmin));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request.getStatus()));
    }
}