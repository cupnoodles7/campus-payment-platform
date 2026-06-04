// Author: Ahana

package com.campus.model;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor

public class SplitExpense {
    private int dueId;          // auto-increment from SQL, never set manually
    private String expenseId;   // UUID string, groups all dues under one split
    private int payerId;
    private int payeeId;
    private double amount;
    private boolean status; // true = settled, false = unsettled
}