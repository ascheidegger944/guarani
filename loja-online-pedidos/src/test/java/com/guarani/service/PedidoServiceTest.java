package com.guarani.service;

import com.guarani.dto.order.OrderDTO;
import com.guarani.model.Order;
import com.guarani.model.OrderStatus;
import com.guarani.repository.OrderRepository;
import com.guarani.service.OrderService;
import com.guarani.utils.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock private OrderRepository pedidoRepository;
    @InjectMocks private OrderService pedidoService;

    @Test
    void criarPedido_DeveRetornarPedidoDTO() {
        Order pedido = TestDataBuilder.buildPedido();
        when(pedidoRepository.save(any())).thenReturn(pedido);

        OrderDTO resultado = pedidoService.createOrder(TestDataBuilder.buildPedidoDTO());

        assertNotNull(resultado);
        verify(pedidoRepository).save(any());
    }

    @Test
    void buscarPorId_ComIdExistente_DeveRetornarPedido() {
        Order pedido = TestDataBuilder.buildPedido();
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        OrderDTO resultado = pedidoService.getOrderById(1L);

        assertNotNull(resultado);
        verify(pedidoRepository).findById(1L);
    }

    @Test
    void listarPedidos_DeveRetornarPage() {
        Page<Order> page = new PageImpl<>(List.of(TestDataBuilder.buildPedido()));
        when(pedidoRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<OrderDTO> resultado = pedidoService.getOrdersByAmountRange(BigDecimal.ONE,BigDecimal.TEN);

        assertNotNull(resultado);
        assertFalse(resultado.size() == 0);
    }
}