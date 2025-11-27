package com.anish.fraud.processor;

import com.anish.fraud.payload.Transaction;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

@Configuration
public class FraudStream {

    @Bean
    public KStream<String, Transaction> processFraud(StreamsBuilder builder) {

        // 1. READ from the source topic
        KStream<String, Transaction> stream = builder.stream("t.commodity.transaction");

        // 2. PROCESS: Filter out innocent transactions
        KStream<String, Transaction> fraudStream = stream
                .filter((key, transaction) -> transaction.getAmount() > 10000) // Rule: Amount > 10k is suspicious
                .peek((key, transaction) -> System.out.println("!! FRAUD DETECTED !!: " + transaction));

        // 3. WRITE to the destination topic
        fraudStream.to("t.commodity.fraud", Produced.with(Serdes.String(), new JsonSerde<>(Transaction.class)));

        return stream;
    }
}