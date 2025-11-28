package com.anish.fraud.consumer;

import com.anish.fraud.payload.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FraudConsumer {

    // This template allows us to send messages to the WebSocket
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "t.commodity.fraud", groupId = "dashboard-group")
    public void listen(Transaction transaction) {
        System.out.println("Received Fraud Alert: " + transaction.getUserId());

        // PUSH TO FRONTEND
        // We send the transaction object to anyone subscribed to "/topic/alerts"
        messagingTemplate.convertAndSend("/topic/alerts", transaction);
    }
}