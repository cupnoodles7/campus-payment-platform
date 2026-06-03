package main.java.com.campus.model;
import java.time.LocalDateTime;

public class Transaction {
    private String txnId;
    private int senderId;
    private int receiverId;
    private double amount;
    private TxnType type;
    private LocalDateTime timestamp;
    private String status;

    public Transaction(String txnId, int senderId, int receiverId,
                       double amount, TxnType type,
                       LocalDateTime timestamp, String status) {
        this.txnId = txnId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters
    public String getTxnId(){
         return txnId;
    }
    public int getSenderId(){
         return senderId; 
    }
    public int getReceiverId(){ 
        return receiverId; 
   }
    public double getAmount(){ 
        return amount; 
    }
    public TxnType getType(){
        return type; 
    }
    public LocalDateTime getTimestamp(){
        return timestamp; 
    }
    public String getStatus(){
        return status; 
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | ₹%.2f | %s → %s | %s | %s",
            txnId, type, amount, senderId, receiverId, status, timestamp);
    }
}