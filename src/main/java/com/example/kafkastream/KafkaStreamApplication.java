package com.example.kafkastream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.config.BindingServiceProperties;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class KafkaStreamApplication {
    private static final Logger log = LoggerFactory.getLogger(KafkaStreamApplication.class);
    
    private final BindingServiceProperties bindingProperties;

    public KafkaStreamApplication(BindingServiceProperties bindingProperties) {
        this.bindingProperties = bindingProperties;
    }

    public static void main(String[] args) {
        SpringApplication.run(KafkaStreamApplication.class, args);
    }

    @PostConstruct
    public void init() {
        log.info("Application started with the following bindings:");
        bindingProperties.getBindings().forEach((name, props) -> {
            log.info("Binding: {}, Destination: {}, Group: {}", 
                     name, props.getDestination(), props.getGroup());
        });
    }
}