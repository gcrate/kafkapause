package com.example.kafkastream.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class LetterNumberService {
    
    private final Set<Integer> processedNumbers = Collections.synchronizedSet(new HashSet<>());
    
    public void addProcessedNumber(Integer number) {
        processedNumbers.add(number);
    }
    
    public boolean isNumberProcessed(int number) {
        return processedNumbers.contains(number);
    }
    
    // Method to get the corresponding number for a letter (A->1, B->2, etc.)
    public int getCorrespondingNumber(String letter) {
        // Assuming uppercase letters
        return letter.charAt(0) - 'A' + 1;
    }
}