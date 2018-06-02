package se.tink.backend.system.cli.interestmap.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Bank {
    private String displayName;
    private Double avgInterestRate;
    @JsonIgnore
    private long numLoans;
    @JsonIgnore
    private long numUsers;

    public static Bank create(String displayName) {
        Bank b = new Bank();
        b.setDisplayName(displayName);
        return b;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Double getAvgInterestRate() {
        return avgInterestRate;
    }

    public void setAvgInterestRate(Double avgInterestRate) {
        this.avgInterestRate = avgInterestRate;
    }

    public long getNumLoans() {
        return numLoans;
    }

    public long getNumUsers() {
        return numUsers;
    }

    public void setNumUsers(long numUsers) {
        this.numUsers = numUsers;
    }

    public void setNumLoans(long numLoans) {
        this.numLoans = numLoans;
    }
}
