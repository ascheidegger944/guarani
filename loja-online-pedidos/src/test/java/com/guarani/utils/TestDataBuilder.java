package com.guarani.utils;

import com.guarani.dto.*;
import com.guarani.dto.order.OrderDTO;
import com.guarani.dto.product.ProductDTO;
import com.guarani.model.*;
import com.guarani.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class TestDataBuilder {

    // Produtos
    public static Produto buildProduto() {
        return Produto.builder()
                .id(1L)
                .codigo("PROD-001")
                .nome("Notebook Dell")
                .descricao("Notebook Dell Inspiron 15")
                .preco(BigDecimal.valueOf(2500.00))
                .quantidadeEstoque(10)
                .categoria("INFORMATICA")
                .ativo(true)
                .dataCriacao(LocalDateTime.now())
                .build();
    }

    public static ProductDTO buildProdutoDTO() {
        ProductDTO dto = new ProductDTO();
        dto.setId(1L);
        dto.setId("PROD-001");
        dto.setName("Notebook Dell");
        dto.setDescription("Notebook Dell Inspiron 15");
        dto.setPrice(BigDecimal.valueOf(2500.00));
        dto.setStockQuantity(10);
        dto.setCategory("INFORMATICA");
        return dto;
    }

    // Pedidos
    public static Order buildPedido() {
        Order pedido = new Order();
        pedido.setId(1L);
        pedido.setId("PED-123456");
        pedido.setStatus(OrderStatus.PENDING);
        pedido.setTotalAmount(BigDecimal.valueOf(150.50));
        pedido.setShippingFee(BigDecimal.valueOf(10.00));
        pedido.setDiscount(BigDecimal.valueOf(5.00));
        pedido.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        pedido.setCreatedAt(LocalDateTime.now());
        return pedido;
    }

    public static OrderDTO buildPedidoDTO() {
        OrderDTO dto = new OrderDTO();
        dto.setId(1L);
        dto.setStatus(StatusPedido.PENDENTE);
        dto.setValorTotal(BigDecimal.valueOf(150.50));
        dto.setValorFrete(BigDecimal.valueOf(10.00));
        dto.setDesconto(BigDecimal.valueOf(5.00));
        dto.setFormaPagamento(FormaPagamento.CARTAO_CREDITO);
        dto.setDataCriacao(LocalDateTime.now());

        // Itens do pedido
        ItemPedidoDTO item = new ItemPedidoDTO();
        item.setProdutoId(1L);
        item.setQuantidade(2);
        item.setPrecoUnitario(BigDecimal.valueOf(50.00));
        dto.setItens(Arrays.asList(item));

        return dto;
    }

    // Usuários
    public static Usuario buildUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("test@guarani.com");
        usuario.setNome("Usuário Teste");
        usuario.setPerfil(PerfilUsuario.CLIENTE);
        usuario.setAtivo(true);
        return usuario;
    }

    // Listas para paginação
    public static List<ProdutoDTO> buildProdutoDTOList() {
        return Arrays.asList(buildProdutoDTO(), buildProdutoDTO());
    }

    public static List<PedidoDTO> buildPedidoDTOList() {
        return Arrays.asList(buildPedidoDTO(), buildPedidoDTO());
    }
}