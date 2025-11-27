package com.anish.fraud.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    private String userId;
    private double amount;
    private String location; // e.g., "Kolkata", "Mumbai"
    private String timestamp;
}