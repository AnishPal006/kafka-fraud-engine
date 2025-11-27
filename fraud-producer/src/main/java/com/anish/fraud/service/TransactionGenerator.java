package com.anish.fraud.service;

import com.anish.fraud.payload.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@EnableScheduling // Tells Spring to allow running tasks periodically
@RequiredArgsConstructor
public class TransactionGenerator {

    // KafkaTemplate is the tool Spring gives us to talk to Kafka
    private final KafkaTemplate<String, Transaction> kafkaTemplate;

    private final Random random = new Random();
    private final List<String> locations = List.of("Kolkata", "Mumbai", "Delhi", "Chennai", "New York", "London");
    private final List<String> users = List.of("User1", "User2", "User3", "User4");

    // This runs every 1000ms (1 second)
    @Scheduled(fixedRate = 1000)
    public void generateTransaction() {
        String user = users.get(random.nextInt(users.size()));
        String location = locations.get(random.nextInt(locations.size()));

        // Random amount between 100 and 100,000
        double amount = 100 + (100000 - 100) * random.nextDouble();

        // Simulating a "Fraud" scenario: 10% chance to have a HUGE amount
        if (random.nextInt(10) == 0) {
            amount = 150000;
        }

        Transaction transaction = new Transaction(
                user,
                Math.round(amount * 100.0) / 100.0, // Round to 2 decimal places
                location,
                LocalDateTime.now().toString()
        );

        // SEND TO KAFKA
        // Topic: "t.commodity.transaction"
        // Key: user (Ensures all User1 transactions go to the same partition)
        // Value: transaction object
        kafkaTemplate.send("t.commodity.transaction", user, transaction);

        System.out.println("Generated: " + transaction);
    }
}