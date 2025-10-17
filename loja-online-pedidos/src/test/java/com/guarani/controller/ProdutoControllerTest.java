package com.guarani.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guarani.dto.order.OrderDTO;
import com.guarani.dto.product.ProductDTO;
import com.guarani.service.OrderService;
import com.guarani.service.ProductService;
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

@WebMvcTest(ProductController.class)
class ProdutoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private ProductService produtoService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void criarProduto_DeveRetornarCreated() throws Exception {
        ProductDTO produtoDTO = TestDataBuilder.buildProdutoDTO();
        when(produtoService.createProduct(any())).thenReturn(produtoDTO);

        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        verify(produtoService).createProduct(any());
    }

    @Test
    @WithMockUser
    void buscarPorId_DeveRetornarProduto() throws Exception {
        ProductDTO produtoDTO = TestDataBuilder.buildProdutoDTO();
        when(produtoService.getProductById(1L)).thenReturn(produtoDTO);

        mockMvc.perform(get("/api/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(produtoService).getProductById(1L);
    }

    @Test
    @WithMockUser
    void listarProdutos_DeveRetornarListaPaginada() throws Exception {
        Page<ProductDTO> page = new PageImpl<>(TestDataBuilder.buildProdutoDTOList());
        when(produtoService.getAllProducts(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(produtoService).getAllProducts(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void excluirProduto_DeveRetornarNoContent() throws Exception {
        doNothing().when(produtoService).deleteProduct(1L);

        mockMvc.perform(delete("/api/produtos/1"))
                .andExpect(status().isNoContent());

        verify(produtoService).deleteProduct(1L);
    }
}