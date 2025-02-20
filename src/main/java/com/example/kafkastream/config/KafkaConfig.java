package com.example.kafkastream.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binding.BindingsLifecycleController;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {
    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);
    
    private final BindingsLifecycleController bindingsController;
    private final BindingServiceProperties bindingServiceProperties;
    
    public KafkaConfig(BindingsLifecycleController bindingsController, 
                     BindingServiceProperties bindingServiceProperties) {
        this.bindingsController = bindingsController;
        this.bindingServiceProperties = bindingServiceProperties;
        log.info("KafkaConfig initialized with bindings: {}", 
                bindingServiceProperties.getBindings().keySet());
    }
    
    /**
     * Pauses the consumer for a specific binding name
     */
    public void pauseConsumer(String bindingName) {
        log.info("Attempting to pause binding: {}", bindingName);
        try {
            bindingsController.pause(bindingName);
            log.info("Successfully paused binding: {}", bindingName);
        } catch (Exception e) {
            log.error("Failed to pause binding {}: {}", bindingName, e.getMessage(), e);
        }
    }
    
    /**
     * Resumes the consumer for a specific binding name
     */
    public void resumeConsumer(String bindingName) {
        log.info("Attempting to resume binding: {}", bindingName);
        try {
            bindingsController.resume(bindingName);
            log.info("Successfully resumed binding: {}", bindingName);
        } catch (Exception e) {
            log.error("Failed to resume binding {}: {}", bindingName, e.getMessage(), e);
        }
    }
    
    /**
     * Gets all available bindings with their destinations
     */
    public Map<String, String> getAvailableBindings() {
        Map<String, String> result = new HashMap<>();
        
        bindingServiceProperties.getBindings().forEach((name, props) -> {
            result.put(name, props.getDestination());
        });
        
        return result;
    }
}