package com.restaurante.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    // RestTemplate es la herramienta de Spring para que el backend Java
    // pueda "consumir" otras APIs, igual que Retrofit lo hace en Android
    // o el ApiClient en JavaFX - pero aqui es Java hablando con Python.
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
