package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanOverviewEntity {
    private String customerId;
    private String loanNumber;
    private double balance;
    private double interestRate;
    private String id;
    private String loanType;
    private NameEntity loanName;

    public String getCustomerId() {
        return customerId;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    public double getBalance() {
        return balance;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public String getId() {
        return id;
    }

    public String getLoanType() {
        return loanType;
    }

    public NameEntity getLoanName() {
        return loanName;
    }
}
