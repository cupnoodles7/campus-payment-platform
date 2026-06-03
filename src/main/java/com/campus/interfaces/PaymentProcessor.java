// Author: Ahana
// student-to-campus service payment

package com.campus.interfaces;

import com.campus.exception.PaymentFailedException;

@FunctionalInterface
public interface PaymentProcessor {
    void campus(int sID, double amount) throws PaymentFailedException;
}