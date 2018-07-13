package se.tink.backend.aggregation.agents.brokers.avanza.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import se.tink.backend.system.rpc.Transaction;
import se.tink.backend.system.rpc.TransactionTypes;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {
    private TransactionalAccountEntity account;
    private String currency;
    private String description;
    private double amount;
    private String transactionType;
    private String verificationDate;
    private String id;
    private OrderbookEntity orderbook;
    private double price;
    private double volume;
    private double sum;
    private String noteId;

    public TransactionalAccountEntity getAccount() {
        return account;
    }

    public void setAccount(TransactionalAccountEntity account) {
        this.account = account;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getVerificationDate() {
        return verificationDate;
    }

    public void setVerificationDate(String verificationDate) {
        this.verificationDate = verificationDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OrderbookEntity getOrderbook() {
        return orderbook;
    }

    public void setOrderbook(OrderbookEntity orderbook) {
        this.orderbook = orderbook;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public Transaction toTinkTransaction() {
        Transaction transaction = new Transaction();
        transaction.setAmount(getAmount());
        transaction.setDescription(getDescription());
        transaction.setDate(java.sql.Date.valueOf(LocalDate.parse(getVerificationDate())));
        transaction.setType(TransactionTypes.TRANSFER);

        return transaction;
    }
}
