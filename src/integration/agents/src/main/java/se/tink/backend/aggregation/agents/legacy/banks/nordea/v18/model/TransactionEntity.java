package se.tink.backend.aggregation.agents.banks.nordea.v18.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {
    private Map<String, Object> billedTransaction = new HashMap<String, Object>();
    // Two pending flags, one for regular transactions and one for credit card transactions.
    private Map<String, Object> coverReservationTransaction = new HashMap<String, Object>();
    private Map<String, Object> isCoverReservationTransaction = new HashMap<String, Object>();
    private Map<String, Object> transactionAmount = new HashMap<String, Object>();
    private Map<String, Object> transactionCounterpartyName = new HashMap<String, Object>();
    private Map<String, Object> transactionCurrency = new HashMap<String, Object>();
    private Map<String, Object> transactionDate = new HashMap<String, Object>();
    private Map<String, Object> transactionEntryType = new HashMap<String, Object>();
    private Map<String, Object> transactionKey = new HashMap<String, Object>();
    private Map<String, Object> transactionText = new HashMap<String, Object>();

    public Map<String, Object> getBilledTransaction() {
        return billedTransaction;
    }

    public Map<String, Object> getCoverReservationTransaction() {
        return coverReservationTransaction;
    }

    public Map<String, Object> getIsCoverReservationTransaction() {
        return isCoverReservationTransaction;
    }

    public Map<String, Object> getTransactionAmount() {
        return transactionAmount;
    }

    public Map<String, Object> getTransactionCounterpartyName() {
        return transactionCounterpartyName;
    }

    public Map<String, Object> getTransactionCurrency() {
        return transactionCurrency;
    }

    public Map<String, Object> getTransactionDate() {
        return transactionDate;
    }

    public Map<String, Object> getTransactionEntryType() {
        return transactionEntryType;
    }

    public Map<String, Object> getTransactionKey() {
        return transactionKey;
    }

    public Map<String, Object> getTransactionText() {
        return transactionText;
    }

    public void setBilledTransaction(Map<String, Object> billedTransaction) {
        this.billedTransaction = billedTransaction;
    }

    public void setCoverReservationTransaction(Map<String, Object> coverReservationTransaction) {
        this.coverReservationTransaction = coverReservationTransaction;
    }

    public void setIsCoverReservationTransaction(
            Map<String, Object> isCoverReservationTransaction) {
        this.isCoverReservationTransaction = isCoverReservationTransaction;
    }

    public void setTransactionAmount(Map<String, Object> transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public void setTransactionCounterpartyName(Map<String, Object> transactionCounterpartyName) {
        this.transactionCounterpartyName = transactionCounterpartyName;
    }

    public void setTransactionCurrency(Map<String, Object> transactionCurrency) {
        this.transactionCurrency = transactionCurrency;
    }

    public void setTransactionDate(Map<String, Object> transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setTransactionEntryType(Map<String, Object> transactionEntryType) {
        this.transactionEntryType = transactionEntryType;
    }

    public void setTransactionKey(Map<String, Object> transactionKey) {
        this.transactionKey = transactionKey;
    }

    public void setTransactionText(Map<String, Object> transactionText) {
        this.transactionText = transactionText;
    }
}
