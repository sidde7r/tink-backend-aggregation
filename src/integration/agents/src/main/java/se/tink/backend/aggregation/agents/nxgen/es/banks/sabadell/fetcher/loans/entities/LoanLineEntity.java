package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanLineEntity {
    private AccountEntity account;
    private String contract;
    private LoanEntity line;
    private String loanType;
    private String owner;

    public AccountEntity getAccount() {
        return account;
    }

    public String getContract() {
        return contract;
    }

    public String getOwner() {
        return owner;
    }
}
