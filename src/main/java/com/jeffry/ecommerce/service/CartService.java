package com.jeffry.ecommerce.service;

import com.jeffry.ecommerce.dto.CartItemRequest;
import com.jeffry.ecommerce.dto.CartItemResponse;
import com.jeffry.ecommerce.dto.CartResponse;
import com.jeffry.ecommerce.entity.CartItem;
import com.jeffry.ecommerce.entity.Product;
import com.jeffry.ecommerce.entity.User;
import com.jeffry.ecommerce.repository.CartItemRepository;
import com.jeffry.ecommerce.repository.ProductRepository;
import com.jeffry.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    public CartResponse getCart(String userEmail) {
        User user = getUser(userEmail);
        List<CartItem> items = cartItemRepository.findByUserId(user.getId());
        return toCartResponse(items);
    }

    public CartResponse addItem(String userEmail, CartItemRequest request) {
        User user = getUser(userEmail);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (product.getStockQty() < request.getQuantity()) {
            throw new RuntimeException("Estoque insuficiente");
        }

        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(user.getId(), product.getId())
                .orElse(CartItem.builder()
                        .user(user)
                        .product(product)
                        .quantity(0)
                        .build());

        cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        cartItemRepository.save(cartItem);

        return getCart(userEmail);
    }

    public CartResponse updateItem(String userEmail, Long itemId, CartItemRequest request) {
        User user = getUser(userEmail);
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item não encontrado no carrinho"));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Item não pertence ao usuário");
        }

        if (cartItem.getProduct().getStockQty() < request.getQuantity()) {
            throw new RuntimeException("Estoque insuficiente");
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        return getCart(userEmail);
    }

    public void removeItem(String userEmail, Long itemId) {
        User user = getUser(userEmail);
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item não encontrado no carrinho"));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Item não pertence ao usuário");
        }

        cartItemRepository.delete(cartItem);
    }

    public void clearCart(String userEmail) {
        User user = getUser(userEmail);
        cartItemRepository.deleteByUserId(user.getId());
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    private CartResponse toCartResponse(List<CartItem> items) {
        List<CartItemResponse> itemResponses = items.stream()
                .map(item -> CartItemResponse.builder()
                        .id(item.getId())
                        .product(productService.toResponse(item.getProduct()))
                        .quantity(item.getQuantity())
                        .subtotal(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        BigDecimal total = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .items(itemResponses)
                .total(total)
                .build();
    }
}