package com.campus.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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