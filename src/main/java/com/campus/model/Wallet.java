package com.campus.model;
public class Wallet {

    private int walletId;
    private int studentId;
    private double balance;
    private double dailyTransferLimit;
    private double balanceCap;
    private double todayTransferred;

    public Wallet() {
    }

    public Wallet(int walletId, int studentId, double balance,
                  double dailyTransferLimit, double balanceCap, double todayTransferred) {
        this.walletId = walletId;
        this.studentId = studentId;
        this.balance = balance;
        this.dailyTransferLimit = dailyTransferLimit;
        this.balanceCap = balanceCap;
        this.todayTransferred = todayTransferred;
    }

    public int getWalletId() {
        return walletId;
    }

    public void setWalletId(int walletId) {
        this.walletId = walletId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getDailyTransferLimit() {
        return dailyTransferLimit;
    }

    public void setDailyTransferLimit(double dailyTransferLimit) {
        this.dailyTransferLimit = dailyTransferLimit;
    }

    public double getBalanceCap() {
        return balanceCap;
    }

    public void setBalanceCap(double balanceCap) {
        this.balanceCap = balanceCap;
    }

    public double getTodayTransferred() {
        return todayTransferred;
    }

    public void setTodayTransferred(double todayTransferred) {
        this.todayTransferred = todayTransferred;
    }

    @Override
    public String toString() {
        return "Wallet{walletId=" + walletId + ", studentId=" + studentId
                + ", balance=" + balance + ", dailyTransferLimit=" + dailyTransferLimit
                + ", balanceCap=" + balanceCap + ", todayTransferred=" + todayTransferred + '}';
    }
}
