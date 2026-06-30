package com.jeffry.ecommerce.service;

import com.jeffry.ecommerce.dto.OrderItemResponse;
import com.jeffry.ecommerce.dto.OrderResponse;
import com.jeffry.ecommerce.entity.*;
import com.jeffry.ecommerce.exception.BusinessException;
import com.jeffry.ecommerce.exception.ResourceNotFoundException;
import com.jeffry.ecommerce.repository.CartItemRepository;
import com.jeffry.ecommerce.repository.OrderRepository;
import com.jeffry.ecommerce.repository.ProductRepository;
import com.jeffry.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    private static final Map<Order.Status, Set<Order.Status>> VALID_TRANSITIONS = new EnumMap<>(Order.Status.class);
    static {
        VALID_TRANSITIONS.put(Order.Status.PENDING, Set.of(Order.Status.CONFIRMED, Order.Status.CANCELLED));
        VALID_TRANSITIONS.put(Order.Status.CONFIRMED, Set.of(Order.Status.SHIPPED, Order.Status.CANCELLED));
        VALID_TRANSITIONS.put(Order.Status.SHIPPED, Set.of(Order.Status.DELIVERED));
        VALID_TRANSITIONS.put(Order.Status.DELIVERED, Set.of());
        VALID_TRANSITIONS.put(Order.Status.CANCELLED, Set.of());
    }

    @Transactional
    public OrderResponse createFromCart(String userEmail) {
        User user = getUser(userEmail);
        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());

        if (cartItems.isEmpty()) {
            throw new BusinessException("Carrinho está vazio");
        }

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new java.util.ArrayList<>();

        Order order = Order.builder()
                .user(user)
                .status(Order.Status.PENDING)
                .build();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            if (product.getStockQty() < cartItem.getQuantity()) {
                throw new BusinessException("Estoque insuficiente para o produto: " + product.getName());
            }

            product.setStockQty(product.getStockQty() - cartItem.getQuantity());
            productRepository.save(product);

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(subtotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            orderItems.add(orderItem);
        }

        order.setTotal(total);
        order.setItems(orderItems);
        order = orderRepository.save(order);

        cartItemRepository.deleteByUserId(user.getId());

        return toResponse(order);
    }

    public List<OrderResponse> findByUser(String userEmail) {
        User user = getUser(userEmail);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse findById(Long id, String userEmail, boolean isAdmin) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));

        if (!isAdmin) {
            User user = getUser(userEmail);
            if (!order.getUser().getId().equals(user.getId())) {
                throw new BusinessException("Pedido não pertence ao usuário");
            }
        }

        return toResponse(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, String newStatusStr) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));

        Order.Status newStatus;
        try {
            newStatus = Order.Status.valueOf(newStatusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Status inválido: " + newStatusStr);
        }

        Order.Status currentStatus = order.getStatus();

        if (!VALID_TRANSITIONS.get(currentStatus).contains(newStatus)) {
            throw new BusinessException("Transição inválida de " + currentStatus + " para " + newStatus);
        }

        if (newStatus == Order.Status.CANCELLED) {
            restoreStock(order);
        }

        order.setStatus(newStatus);
        order = orderRepository.save(order);

        return toResponse(order);
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQty(product.getStockQty() + item.getQuantity());
            productRepository.save(product);
        }
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .product(productService.toResponse(item.getProduct()))
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus().name())
                .total(order.getTotal())
                .items(items)
                .createdAt(order.getCreatedAt())
                .build();
    }
}