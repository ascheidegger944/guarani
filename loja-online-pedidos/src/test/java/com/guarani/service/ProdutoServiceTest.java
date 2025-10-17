package com.guarani.service;

import com.guarani.dto.ProdutoDTO;
import com.guarani.entity.Produto;
import com.guarani.repository.ProdutoRepository;
import com.guarani.service.impl.ProdutoServiceImpl;
import com.guarani.utils.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock private ProdutoRepository produtoRepository;
    @InjectMocks private ProdutoServiceImpl produtoService;

    @Test
    void criarProduto_DeveRetornarProdutoDTO() {
        Produto produto = TestDataBuilder.buildProduto();
        when(produtoRepository.save(any())).thenReturn(produto);

        ProdutoDTO resultado = produtoService.criarProduto(TestDataBuilder.buildProdutoDTO());

        assertNotNull(resultado);
        verify(produtoRepository).save(any());
    }

    @Test
    void buscarPorId_ComIdExistente_DeveRetornarProduto() {
        Produto produto = TestDataBuilder.buildProduto();
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        ProdutoDTO resultado = produtoService.buscarPorId(1L);

        assertNotNull(resultado);
        verify(produtoRepository).findById(1L);
    }
}