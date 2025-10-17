package com.guarani.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guarani.dto.order.OrderDTO;
import com.guarani.model.OrderStatus;
import com.guarani.service.OrderService;
import com.guarani.utils.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class PedidoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private OrderService pedidoService;

    @Test
    @WithMockUser(roles = "CLIENTE")
    void criarPedido_DeveRetornarSucesso() throws Exception {
        OrderDTO pedidoDTO = TestDataBuilder.buildPedidoDTO();
        when(pedidoService.createOrder(any())).thenReturn(pedidoDTO);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());

        verify(pedidoService).createOrder(any());
    }

    @Test
    @WithMockUser
    void buscarPorId_DeveRetornarPedido() throws Exception {
        PedidoDTO pedidoDTO = TestDataBuilder.buildPedidoDTO();
        when(pedidoService.getOrderById(1L)).thenReturn(pedidoDTO);

        mockMvc.perform(get("/api/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(pedidoService).getOrderById(1L);
    }

    @Test
    @WithMockUser
    void listarPedidos_DeveRetornarListaPaginada() throws Exception {
        Page<PedidoDTO> page = new PageImpl<>(TestDataBuilder.buildPedidoDTOList());
        when(pedidoService.getOrderById(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(pedidoService).getAllOrders();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void excluirPedido_DeveRetornarNoContent() throws Exception {
        doNothing().when(pedidoService).deleteOrder(1L);

        mockMvc.perform(delete("/api/pedidos/1"))
                .andExpect(status().isNoContent());

        verify(pedidoService).deleteOrder(1L);
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void excluirPedido_SemPermissao_DeveRetornarForbidden() throws Exception {
        mockMvc.perform(delete("/api/pedidos/1"))
                .andExpect(status().isForbidden());
    }
}