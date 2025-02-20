package com.example.kafkastream.consumer;

import com.example.kafkastream.config.KafkaConfig;
import com.example.kafkastream.model.Letter;
import com.example.kafkastream.service.LetterNumberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class LetterConsumer {
    
    private final LetterNumberService letterNumberService;
    private final KafkaConfig kafkaConfig;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    // The binding name used by Spring Cloud Stream
    private static final String BINDING_NAME = "processLetter-in-0";
    private static final int MAX_RETRIES = 5;
    private int retryCount = 0;
    
    @Bean
    public Consumer<Letter> processLetter() {
        return letter -> {
            log.info("Received letter: {}", letter.getValue());
            
            // Get corresponding number for the letter
            int correspondingNumber = letterNumberService.getCorrespondingNumber(letter.getValue());
            
            // Check if the corresponding number has been processed
            if (!letterNumberService.isNumberProcessed(correspondingNumber)) {
                if (retryCount >= MAX_RETRIES) {
                    log.error("ISSUE: Corresponding number {} for letter {} could not be processed after {} retries. Moving to next message.", 
                              correspondingNumber, letter.getValue(), MAX_RETRIES);
                    retryCount = 0; // Reset retry count for the next message
                    return; // Skip further processing
                }
                
                log.info("Corresponding number {} for letter {} has not been processed yet. Pausing consumer... Retry count: {}", 
                          correspondingNumber, letter.getValue(), retryCount + 1);
                
                try {
                    // Pause the consumer using Spring Cloud Stream binding lifecycle
                    kafkaConfig.pauseConsumer(BINDING_NAME);
                    
                    // Schedule resume after 1 minute
                    scheduler.schedule(() -> {
                        log.info("Scheduled resume after 1 minute pause");
                        kafkaConfig.resumeConsumer(BINDING_NAME);
                    }, 1, TimeUnit.MINUTES);
                    
                    // Increment retry count
                    retryCount++;
                    
                } catch (Exception e) {
                    log.error("Error managing consumer lifecycle: {}", e.getMessage(), e);
                }
                
                return; // Skip further processing
            }
            
            // Process the letter if corresponding number was found
            log.info("Processed letter: {} (corresponding to number: {})", 
                     letter.getValue(), correspondingNumber);
            retryCount = 0; // Reset retry count after successful processing
        };
    }
}