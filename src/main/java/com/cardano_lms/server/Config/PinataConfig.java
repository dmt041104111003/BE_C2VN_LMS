package com.cardano_lms.server.Config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pinata.Pinata;

@Configuration
public class PinataConfig {

    @Value("${PINATA_API_KEY}")
    private String apiKey;

    @Value("${PINATA_SECRET_KEY}")
    private String secretKey;

    @Bean
    public Pinata pinata() {
        return new Pinata(apiKey, secretKey);
    }
}
