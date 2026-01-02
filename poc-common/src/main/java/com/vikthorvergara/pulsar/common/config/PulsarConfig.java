package com.vikthorvergara.pulsar.common.config;

import org.apache.pulsar.client.api.PulsarClient;
import org.springframework.boot.autoconfigure.pulsar.PulsarProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.pulsar.core.DefaultPulsarClientFactory;
import org.springframework.pulsar.core.PulsarAdministration;

@Configuration
public class PulsarConfig {

    @Bean
    public PulsarAdministration pulsarAdministration(PulsarClient pulsarClient) {
        return new PulsarAdministration(pulsarClient);
    }

    @Bean
    public PulsarClient pulsarClient(PulsarProperties properties) throws Exception {
        var factory = new DefaultPulsarClientFactory(properties.getClient());
        return factory.createClient();
    }
}
