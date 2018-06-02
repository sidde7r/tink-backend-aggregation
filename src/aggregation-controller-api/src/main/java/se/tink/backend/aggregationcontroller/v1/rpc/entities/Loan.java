package se.tink.backend.aggregationcontroller.v1.rpc.entities;

import java.util.Date;
import java.util.UUID;

public class Loan {
    public enum Type {
        MORTGAGE, BLANCO, MEMBERSHIP, VEHICLE, LAND, STUDENT, OTHER
    }

    private UUID accountId;
    private UUID id;
    private UUID userId;
    private UUID credentialsId;
    private Double initialBalance;
    private Date initialDate;
    private Integer numMonthsBound;
    private String name;
    private Double interest;
    private Double balance;
    private Double amortized;
    private Date nextDayOfTermsChange;
    private String serializedLoanResponse;
    private Date updated;
    private String providerName;
    private String type;
    private String loanNumber;
    private Double monthlyAmortization;
    private LoanDetails loanDetails;
    private Boolean userModifiedType;

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(UUID credentialsId) {
        this.credentialsId = credentialsId;
    }

    public Double getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(Double initialBalance) {
        this.initialBalance = initialBalance;
    }

    public Date getInitialDate() {
        return initialDate;
    }

    public void setInitialDate(Date initialDate) {
        this.initialDate = initialDate;
    }

    public Integer getNumMonthsBound() {
        return numMonthsBound;
    }

    public void setNumMonthsBound(Integer numMonthsBound) {
        this.numMonthsBound = numMonthsBound;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Double getAmortized() {
        return amortized;
    }

    public void setAmortized(Double amortized) {
        this.amortized = amortized;
    }

    public Date getNextDayOfTermsChange() {
        return nextDayOfTermsChange;
    }

    public void setNextDayOfTermsChange(Date nextDayOfTermsChange) {
        this.nextDayOfTermsChange = nextDayOfTermsChange;
    }

    public String getSerializedLoanResponse() {
        return serializedLoanResponse;
    }

    public void setSerializedLoanResponse(String serializedLoanResponse) {
        this.serializedLoanResponse = serializedLoanResponse;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    public void setLoanNumber(String loanNumber) {
        this.loanNumber = loanNumber;
    }

    public Double getMonthlyAmortization() {
        return monthlyAmortization;
    }

    public void setMonthlyAmortization(Double monthlyAmortization) {
        this.monthlyAmortization = monthlyAmortization;
    }

    public LoanDetails getLoanDetails() {
        return loanDetails;
    }

    public void setLoanDetails(LoanDetails loanDetails) {
        this.loanDetails = loanDetails;
    }

    public Boolean getUserModifiedType() {
        return userModifiedType;
    }

    public void setUserModifiedType(Boolean userModifiedType) {
        this.userModifiedType = userModifiedType;
    }
}
