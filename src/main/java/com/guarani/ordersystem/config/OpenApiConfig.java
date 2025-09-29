package com.guarani.ordersystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${guarani.openapi.dev-url:http://localhost:8080}")
    private String devUrl;

    @Value("${guarani.openapi.prod-url:https://guarani-production.com}")
    private String prodUrl;

    @Bean
    public OpenAPI guaraniOrderSystemAPI() {
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("Servidor de Desenvolvimento");

        Server prodServer = new Server();
        prodServer.setUrl(prodUrl);
        prodServer.setDescription("Servidor de Produção");

        Contact contact = new Contact();
        contact.setEmail("suporte@guarani.com");
        contact.setName("Equipe Guarani");
        contact.setUrl("https://www.guarani.com");

        License mitLicense = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Guarani Order System API")
                .version("v1.0.0")
                .contact(contact)
                .description("API RESTful para o sistema de gerenciamento de pedidos Guarani")
                .termsOfService("https://www.guarani.com/terms")
                .license(mitLicense);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}