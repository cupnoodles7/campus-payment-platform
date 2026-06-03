// Author: Ahana
// student-to-student money movement

package main.java.com.campus.interfaces;

@FunctionalInterface
public interface TransferHandler {
    void transfer(int from, int to, double amt);
}