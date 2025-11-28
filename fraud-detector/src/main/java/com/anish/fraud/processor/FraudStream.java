package com.anish.fraud.processor;

import com.anish.fraud.payload.Transaction;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.time.Duration;

@Configuration
public class FraudStream {

    @Bean
    public KStream<String, Transaction> processFraud(StreamsBuilder builder) {

        // 1. INPUT: Read the stream
        KStream<String, Transaction> stream = builder.stream("t.commodity.transaction");

        // --- RULE 1: Simple Amount Check (> 10,000) ---
        stream.filter((key, transaction) -> transaction.getAmount() > 10000)
                .peek((key, t) -> System.out.println("!! FRAUD (Large Amount) !!: " + t))
                .to("t.commodity.fraud", Produced.with(Serdes.String(), new JsonSerde<>(Transaction.class)));


        // --- RULE 2: Velocity Check (> 5 transactions per minute) ---

        // Step A: Group by User ID (The Key)
        KTable<Windowed<String>, Long> windowedCounts = stream
                .groupByKey() // Group transactions by the key (userId)
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1))) // 1-minute fixed window
                .count(); // Count how many in that window

        // Step B: Filter and Convert back to Stream
        windowedCounts.toStream()
                .filter((windowedKey, count) -> count > 5) // TRIGGER: If count > 5
                .peek((windowedKey, count) -> {
                    String user = windowedKey.key();
                    System.out.println("!! FRAUD (High Frequency) !!: User " + user + " made " + count + " transactions in 1 minute!");
                })
                // Step C: Send a simple alert message to the fraud topic
                // (We have to construct a dummy transaction or special alert object here.
                //  For simplicity, we will just print to console for now, or map to a new object)
                .map((windowedKey, count) -> {
                    Transaction alert = new Transaction();
                    alert.setUserId(windowedKey.key());
                    alert.setAmount(0); // Dummy amount
                    alert.setLocation("System Alert");
                    alert.setTimestamp("Velocity Rule: " + count + " attempts");
                    return KeyValue.pair(windowedKey.key(), alert);
                })
                .to("t.commodity.fraud", Produced.with(Serdes.String(), new JsonSerde<>(Transaction.class)));

        return stream;
    }
}