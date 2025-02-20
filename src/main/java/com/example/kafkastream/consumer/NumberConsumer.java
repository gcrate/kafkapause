package com.example.kafkastream.consumer;

import com.example.kafkastream.model.Number;
import com.example.kafkastream.service.LetterNumberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class NumberConsumer {
    
    private final LetterNumberService letterNumberService;
    
    @Bean
    public Consumer<Number> processNumber() {
        return number -> {
            log.info("Received number: {}", number);
            letterNumberService.addProcessedNumber(number.getValue());
            log.info("Processed number: {}", number);
        };
    }
}