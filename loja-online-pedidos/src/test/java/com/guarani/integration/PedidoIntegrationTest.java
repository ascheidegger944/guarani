package com.guarani.integration;

import com.guarani.config.TestConfig;
import com.guarani.dto.order.OrderDTO;
import com.guarani.utils.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = TestConfig.class)
class PedidoIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void fluxoCompletoPedido_DeveFuncionar() {
        // Configurar headers de autenticação
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth("user", "password");

        // Testar criação de pedido
        OrderDTO pedidoDTO = TestDataBuilder.buildPedidoDTO();
        HttpEntity<OrderDTO> request = new HttpEntity<>(pedidoDTO, headers);

        ResponseEntity<OrderDTO> response = restTemplate.exchange(
                "/api/pedidos", HttpMethod.POST, request, OrderDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}