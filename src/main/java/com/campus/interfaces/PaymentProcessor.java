// Author: Ahana
// student-to-campus service payment

package main.java.com.campus.interfaces;

@FunctionalInterface
public interface PaymentProcessor {
    campus(int sID, double amount) throws PaymentFailedException;
}