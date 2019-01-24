package se.tink.backend.aggregation.agents.brokers.lysa.model;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.agents.models.Transaction;
import java.util.Date;
import java.util.Objects;

@JsonObject
public class TransactionEntity {
    private String accountId;
    private double amount;
    private String bank;
    private Date booked;
    private String externalBankAccount;
    private String type;
    private String contractNoteId;
    private String isin;
    private Double volume;
    private String depositChannel;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public Date getBooked() {
        return booked;
    }

    public void setBooked(Date booked) {
        this.booked = booked;
    }

    public String getExternalBankAccount() {
        return externalBankAccount;
    }

    public void setExternalBankAccount(String externalBankAccount) {
        this.externalBankAccount = externalBankAccount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContractNoteId() {
        return contractNoteId;
    }

    public void setContractNoteId(String contractNoteId) {
        this.contractNoteId = contractNoteId;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    public String getDepositChannel() {
        return depositChannel;
    }

    public void setDepositChannel(String depositChannel) {
        this.depositChannel = depositChannel;
    }

    public Transaction toTransaction() {
        Transaction t = new Transaction();

        t.setDate(booked);

        switch (type) {
            case "DEPOSIT":
                t.setDescription("Insättning");
                t.setAmount(amount);
                break;
            case "WITHDRAWAL":
                t.setDescription("Uttag");
                t.setAmount(-amount);
                break;
        }

        return t;
    }

    public boolean isValidTransaction() {
        return (Objects.equals(type, "DEPOSIT") || Objects.equals(type, "WITHDRAWAL"));
    }
}
