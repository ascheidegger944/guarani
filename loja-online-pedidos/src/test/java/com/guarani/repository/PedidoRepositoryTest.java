package com.guarani.repository;

import com.guarani.config.TestConfig;
import com.guarani.model.Order;
import com.guarani.model.OrderStatus;
import com.guarani.model.User;
import com.guarani.utils.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestConfig.class)
class PedidoRepositoryTest {

    @Autowired private TestEntityManager entityManager;
    @Autowired private OrderRepository pedidoRepository;
    @Autowired private UserRepository usuarioRepository;

    @Test
    void salvarPedido_DevePersistirCorretamente() {
        User usuario = TestDataBuilder.();
        entityManager.persistAndFlush(usuario);

        Order pedido = TestDataBuilder.buildPedido();
        pedido.setUser(usuario);

        Order salvo = pedidoRepository.save(pedido);

        assertNotNull(salvo.getId());
        assertEquals(pedido.getId(), salvo.getId());
    }

    @Test
    void buscarPorStatus_DeveRetornarPedidos() {
        User usuario = TestDataBuilder.buildUsuario();
        entityManager.persistAndFlush(usuario);

        Order pedido = TestDataBuilder.buildPedido();
        pedido.setUser(usuario);
        entityManager.persistAndFlush(pedido);

        Page<Order> resultado = pedidoRepository.findByStatus(
                OrderStatus.PENDING, PageRequest.of(0, 10));

        assertNotNull(resultado);
        assertTrue(resultado.getTotalElements() > 0);
    }
}