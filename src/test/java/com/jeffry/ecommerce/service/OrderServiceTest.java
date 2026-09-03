package com.jeffry.ecommerce.service;

import com.jeffry.ecommerce.dto.OrderResponse;
import com.jeffry.ecommerce.entity.CartItem;
import com.jeffry.ecommerce.entity.Order;
import com.jeffry.ecommerce.entity.OrderItem;
import com.jeffry.ecommerce.entity.Product;
import com.jeffry.ecommerce.entity.User;
import com.jeffry.ecommerce.exception.BusinessException;
import com.jeffry.ecommerce.exception.ResourceNotFoundException;
import com.jeffry.ecommerce.repository.CartItemRepository;
import com.jeffry.ecommerce.repository.OrderRepository;
import com.jeffry.ecommerce.repository.ProductRepository;
import com.jeffry.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para o workflow de pedidos: criação a partir do carrinho,
 * baixa/estorno de estoque e transições de status válidas e inválidas.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("maria@email.com")
                .role(User.Role.USER)
                .build();

        product = Product.builder()
                .id(10L)
                .name("Teclado mecânico")
                .price(BigDecimal.valueOf(250))
                .stockQty(5)
                .build();

        lenient().when(userRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(user));
        lenient().when(productService.toResponse(any(Product.class))).thenReturn(null);
    }

    @Test
    void createFromCart_deveLancarExcecaoQuandoCarrinhoVazio() {
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.createFromCart("maria@email.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Carrinho está vazio");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createFromCart_deveLancarExcecaoQuandoEstoqueInsuficiente() {
        product.setStockQty(1);
        CartItem cartItem = CartItem.builder().id(1L).user(user).product(product).quantity(5).build();

        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));

        assertThatThrownBy(() -> orderService.createFromCart("maria@email.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Estoque insuficiente");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createFromCart_deveDarBaixaNoEstoqueELimparCarrinhoAoConfirmarPedido() {
        CartItem cartItem = CartItem.builder().id(1L).user(user).product(product).quantity(2).build();

        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return order;
        });

        orderService.createFromCart("maria@email.com");

        assertThat(product.getStockQty()).isEqualTo(3); // 5 - 2
        verify(productRepository).save(product);
        verify(cartItemRepository).deleteByUserId(1L);
    }

    @Test
    void updateStatus_devePermitirTransicaoValidaDePendingParaConfirmed() {
        Order order = Order.builder()
                .id(1L)
                .user(user)
                .status(Order.Status.PENDING)
                .total(BigDecimal.valueOf(500))
                .items(List.of())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.updateStatus(1L, "CONFIRMED");

        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void updateStatus_deveLancarExcecaoParaTransicaoInvalida() {
        Order order = Order.builder()
                .id(1L)
                .user(user)
                .status(Order.Status.PENDING)
                .items(List.of())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // PENDING não pode ir direto para DELIVERED
        assertThatThrownBy(() -> orderService.updateStatus(1L, "DELIVERED"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transição inválida");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateStatus_deveLancarExcecaoParaStatusDesconhecido() {
        Order order = Order.builder().id(1L).user(user).status(Order.Status.PENDING).items(List.of()).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, "STATUS_INEXISTENTE"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Status inválido");
    }

    @Test
    void updateStatus_deveRestaurarEstoqueAoCancelarPedido() {
        OrderItem item = OrderItem.builder()
                .id(1L)
                .product(product)
                .quantity(2)
                .unitPrice(product.getPrice())
                .build();

        Order order = Order.builder()
                .id(1L)
                .user(user)
                .status(Order.Status.CONFIRMED)
                .items(List.of(item))
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.updateStatus(1L, "CANCELLED");

        assertThat(product.getStockQty()).isEqualTo(7); // 5 + 2 devolvidos
        verify(productRepository).save(product);
    }

    @Test
    void findById_deveLancarExcecaoQuandoPedidoNaoExiste() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(999L, "maria@email.com", false))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findById_deveLancarExcecaoQuandoPedidoNaoPertenceAoUsuario() {
        User outroUsuario = User.builder().id(2L).email("outro@email.com").build();
        Order order = Order.builder().id(1L).user(outroUsuario).items(List.of()).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.findById(1L, "maria@email.com", false))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não pertence ao usuário");
    }

    @Test
    void findById_devePermitirAdminVerPedidoDeOutroUsuario() {
        User outroUsuario = User.builder().id(2L).email("outro@email.com").build();
        Order order = Order.builder().id(1L).user(outroUsuario).status(Order.Status.PENDING).items(List.of()).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.findById(1L, "qualquer@email.com", true);

        assertThat(response.getId()).isEqualTo(1L);
    }
}
