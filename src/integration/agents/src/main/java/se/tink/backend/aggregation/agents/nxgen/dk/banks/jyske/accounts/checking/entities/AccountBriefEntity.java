package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountBriefEntity {
    protected String regNo;
    protected String accountNo;

    public String getRegNo() {
        return regNo;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }
}
