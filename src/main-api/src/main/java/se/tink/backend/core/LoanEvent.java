package se.tink.backend.core;

import java.util.Date;
import java.util.Map;

public class LoanEvent {
    private String accountId;
    private Date timestamp;
    private Type type;
    private Loan.Type loanType;
    private String title;
    private Double interest;
    private Double balance;
    private Double interestRateChange;
    private String provider;
    private String credentials;
    private Date nextDayOfTermsChange;

    private Map<String, Object> properties;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Loan.Type getLoanType() {
        return loanType;
    }

    public void setLoanType(Loan.Type loanType) {
        this.loanType = loanType;
    }

    public Double getInterest() {
        return interest;
    }

    public void setInterest(Double interest) {
        this.interest = interest;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Double getInterestRateChange() {
        return interestRateChange;
    }

    public void setInterestRateChange(Double interestRateChange) {
        this.interestRateChange = interestRateChange;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public Date getNextDayOfTermsChange() {
        return nextDayOfTermsChange;
    }

    public void setNextDayOfTermsChange(Date nextDayOfTermsChange) {
        this.nextDayOfTermsChange = nextDayOfTermsChange;
    }

    public enum Type {
        INFO,
        EMPTY,
        INTEREST_RATE_DECREASE,
        INTEREST_RATE_INCREASE
    }
}

