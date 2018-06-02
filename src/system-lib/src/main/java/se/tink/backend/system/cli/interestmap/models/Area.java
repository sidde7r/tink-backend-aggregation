package se.tink.backend.system.cli.interestmap.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Area {
    private String identifier;
    private String displayName;
    private Double avgInterestRate;
    private Long avgLoanBalance;
    private Long avgIncome;
    private List<Bank> banks;
    @JsonIgnore
    private long numLoans;
    @JsonIgnore
    private long numUsers;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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

    public Long getAvgLoanBalance() {
        return avgLoanBalance;
    }

    public void setAvgLoanBalance(Long avgLoanBalance) {
        this.avgLoanBalance = avgLoanBalance;
    }

    public Long getAvgIncome() {
        return avgIncome;
    }

    public void setAvgIncome(Long avgIncome) {
        this.avgIncome = avgIncome;
    }

    public List<Bank> getBanks() {
        return banks;
    }

    public void setBanks(List<Bank> banks) {
        this.banks = banks;
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
