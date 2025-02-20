package com.example.kafkastream.controller;

import com.example.kafkastream.config.KafkaConfig;
import com.example.kafkastream.model.Letter;
import com.example.kafkastream.model.Number;
import com.example.kafkastream.service.LetterNumberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class KafkaTestController {

    private final StreamBridge streamBridge;
    private final KafkaConfig kafkaConfig;
    private final LetterNumberService letterNumberService;

    @GetMapping("/send/number/{value}")
    public ResponseEntity<String> sendNumber(@PathVariable Integer value) {
        // Important: Send to the correct binding name, not the topic name directly
        boolean sent = streamBridge.send("processNumber-in-0", new Number(value));
        log.info("Sent number {} to Kafka: {}", value, sent);
        return ResponseEntity.ok("Sent number: " + value + " (success: " + sent + ")");
    }

    @GetMapping("/send/letter/{value}")
    public ResponseEntity<String> sendLetter(@PathVariable String value) {
        // Important: Send to the correct binding name, not the topic name directly
        boolean sent = streamBridge.send("processLetter-in-0", new Letter(value));
        log.info("Sent letter {} to Kafka: {}", value, sent);
        return ResponseEntity.ok("Sent letter: " + value + " (success: " + sent + ")");
    }
    
    @GetMapping("/kafka/status")
    public ResponseEntity<String> getKafkaStatus() {
        StringBuilder status = new StringBuilder("Spring Cloud Stream Bindings:\n");
        
        Map<String, String> bindings = kafkaConfig.getAvailableBindings();
        bindings.forEach((name, destination) -> {
            status.append("Binding name: ").append(name)
                  .append(", Destination: ").append(destination)
                  .append("\n");
        });
        
        status.append("\nProcessed Numbers:\n");
        for (int i = 1; i <= 10; i++) {
            status.append(i).append(": ")
                  .append(letterNumberService.isNumberProcessed(i) ? "Processed" : "Not Processed")
                  .append("\n");
        }
        
        // Add direct test to check if messages can be sent
        try {
            boolean testNumber = streamBridge.send("processNumber-in-0", new Number(999));
            boolean testLetter = streamBridge.send("processLetter-in-0", new Letter("Z"));
            status.append("\nDirect send test:")
                  .append("\nNumber send test: ").append(testNumber)
                  .append("\nLetter send test: ").append(testLetter);
        } catch (Exception e) {
            status.append("\nDirect send test failed: ").append(e.getMessage());
        }
        
        return ResponseEntity.ok(status.toString());
    }
    
    @PostMapping("/kafka/pause/{bindingName}")
    public ResponseEntity<String> pauseBinding(@PathVariable String bindingName) {
        try {
            kafkaConfig.pauseConsumer(bindingName);
            return ResponseEntity.ok("Requested pause for binding: " + bindingName);
        } catch (Exception e) {
            log.error("Error pausing binding: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/kafka/resume/{bindingName}")
    public ResponseEntity<String> resumeBinding(@PathVariable String bindingName) {
        try {
            kafkaConfig.resumeConsumer(bindingName);
            return ResponseEntity.ok("Requested resume for binding: " + bindingName);
        } catch (Exception e) {
            log.error("Error resuming binding: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}