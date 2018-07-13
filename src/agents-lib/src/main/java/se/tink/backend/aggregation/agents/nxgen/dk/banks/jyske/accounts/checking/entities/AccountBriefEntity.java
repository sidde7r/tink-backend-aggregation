package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountBriefEntity {
    protected String regNo;
    private String shadowAccountId;

    public String getShadowAccountId() {
        return shadowAccountId;
    }

    public void setShadowAccountId(String shadowAccountId) {
        this.shadowAccountId = shadowAccountId;
    }

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
