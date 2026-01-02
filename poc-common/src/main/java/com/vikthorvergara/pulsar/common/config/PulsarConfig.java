package com.vikthorvergara.pulsar.common.config;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.pulsar.annotation.EnablePulsar;
import org.springframework.pulsar.config.ConcurrentPulsarListenerContainerFactory;
import org.springframework.pulsar.core.DefaultPulsarConsumerFactory;
import org.springframework.pulsar.core.DefaultPulsarProducerFactory;
import org.springframework.pulsar.core.PulsarConsumerFactory;
import org.springframework.pulsar.core.PulsarProducerFactory;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.pulsar.listener.PulsarContainerProperties;

import java.util.Collections;

@Configuration
@EnablePulsar
public class PulsarConfig {

    @Bean
    public PulsarClient pulsarClient(@Value("${spring.pulsar.client.service-url}") String serviceUrl)
            throws PulsarClientException {
        return PulsarClient.builder()
                .serviceUrl(serviceUrl)
                .build();
    }

    @Bean
    public PulsarProducerFactory pulsarProducerFactory(PulsarClient pulsarClient) {
        return new DefaultPulsarProducerFactory<>(pulsarClient);
    }

    @Bean
    public PulsarTemplate pulsarTemplate(PulsarProducerFactory pulsarProducerFactory) {
        return new PulsarTemplate<>(pulsarProducerFactory);
    }

    @Bean
    public PulsarConsumerFactory pulsarConsumerFactory(PulsarClient pulsarClient) {
        return new DefaultPulsarConsumerFactory<>(pulsarClient, Collections.emptyList());
    }

    @Bean
    public ConcurrentPulsarListenerContainerFactory pulsarListenerContainerFactory(
            PulsarConsumerFactory pulsarConsumerFactory) {
        PulsarContainerProperties containerProperties = new PulsarContainerProperties();
        return new ConcurrentPulsarListenerContainerFactory<>(pulsarConsumerFactory, containerProperties);
    }
}
