// Author: Ahana
// student-to-student money movement

package com.campus.interfaces;

@FunctionalInterface
public interface TransferHandler {
    void handle(int from, int to, double amt);
}