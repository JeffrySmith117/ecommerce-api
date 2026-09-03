package com.jeffry.ecommerce.service;

import com.jeffry.ecommerce.dto.CartItemRequest;
import com.jeffry.ecommerce.dto.CartResponse;
import com.jeffry.ecommerce.entity.CartItem;
import com.jeffry.ecommerce.entity.Product;
import com.jeffry.ecommerce.entity.User;
import com.jeffry.ecommerce.repository.CartItemRepository;
import com.jeffry.ecommerce.repository.ProductRepository;
import com.jeffry.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para as regras de negócio do carrinho de compras:
 * validação de estoque, incremento de quantidade e checagem de posse do item.
 */
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Maria Silva")
                .email("maria@email.com")
                .role(User.Role.USER)
                .build();

        product = Product.builder()
                .id(10L)
                .name("Teclado mecânico")
                .price(BigDecimal.valueOf(250))
                .stockQty(5)
                .build();

        when(userRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(user));
    }

    @Test
    void addItem_deveAdicionarNovoItemQuandoHaEstoqueSuficiente() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(10L);
        request.setQuantity(2);

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.empty());
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of());

        cartService.addItem("maria@email.com", request);

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository, times(1)).save(captor.capture());

        CartItem saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getProduct()).isEqualTo(product);
        assertThat(saved.getQuantity()).isEqualTo(2);
    }

    @Test
    void addItem_deveIncrementarQuantidadeQuandoProdutoJaEstaNoCarrinho() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(10L);
        request.setQuantity(3);

        CartItem existingItem = CartItem.builder()
                .id(99L)
                .user(user)
                .product(product)
                .quantity(2)
                .build();

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.of(existingItem));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(existingItem));
        when(productService.toResponse(product)).thenReturn(null);

        cartService.addItem("maria@email.com", request);

        assertThat(existingItem.getQuantity()).isEqualTo(5);
        verify(cartItemRepository).save(existingItem);
    }

    @Test
    void addItem_deveLancarExcecaoQuandoEstoqueInsuficiente() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(10L);
        request.setQuantity(999);

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addItem("maria@email.com", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Estoque insuficiente");

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void addItem_deveLancarExcecaoQuandoProdutoNaoExiste() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(999L);
        request.setQuantity(1);

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem("maria@email.com", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Produto não encontrado");
    }

    @Test
    void removeItem_deveLancarExcecaoQuandoItemNaoPertenceAoUsuario() {
        User outroUsuario = User.builder().id(2L).email("outro@email.com").build();
        CartItem itemDeOutroUsuario = CartItem.builder()
                .id(50L)
                .user(outroUsuario)
                .product(product)
                .quantity(1)
                .build();

        when(cartItemRepository.findById(50L)).thenReturn(Optional.of(itemDeOutroUsuario));

        assertThatThrownBy(() -> cartService.removeItem("maria@email.com", 50L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não pertence ao usuário");

        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void getCart_deveCalcularTotalCorretamenteParaVariosItens() {
        CartItem item1 = CartItem.builder().id(1L).user(user).product(product).quantity(2).build();

        Product segundoProduto = Product.builder()
                .id(20L)
                .name("Mouse")
                .price(BigDecimal.valueOf(80))
                .stockQty(10)
                .build();
        CartItem item2 = CartItem.builder().id(2L).user(user).product(segundoProduto).quantity(1).build();

        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(item1, item2));
        when(productService.toResponse(any(Product.class))).thenReturn(null);

        CartResponse response = cartService.getCart("maria@email.com");

        // total esperado: (250 * 2) + (80 * 1) = 580
        assertThat(response.getTotal()).isEqualByComparingTo(BigDecimal.valueOf(580));
    }
}
