package com.campus.model;

import lombok.*;
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor

public class Wallet {

    private int walletId;
    private int studentId;
    private double balance;
    private double dailyTransferLimit;
    private double balanceCap;
    private double todayTransferred;

    
}
