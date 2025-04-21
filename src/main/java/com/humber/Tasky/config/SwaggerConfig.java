package com.humber.Tasky.config;

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
public class SwaggerConfig {

    @Value("${tasky.openapi.dev-url}")
    private String devUrl;

    @Value("${tasky.openapi.prod-url}")
    private String prodUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("Server URL in Development environment");

        Server prodServer = new Server();
        prodServer.setUrl(prodUrl);
        prodServer.setDescription("Server URL in Production environment");

        Contact contact = new Contact();
        contact.setEmail("tasky@humber.ca");
        contact.setName("Tasky Team");
        contact.setUrl("https://github.com/dailker/congenial-octo-engine");

        License mitLicense = new License()
                .name("MIT License")
                .url("https://github.com/dailker/congenial-octo-engine/blob/main/LICENSE");

        Info info = new Info()
                .title("Tasky API")
                .version("1.0")
                .contact(contact)
                .description("API documentation for Tasky application")
                .license(mitLicense);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}