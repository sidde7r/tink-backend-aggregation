package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankdataAccountIdEntity {
    private String regNo;
    private String accountNo;
    private String accountName;

    public String getRegNo() {
        return regNo;
    }

    public BankdataAccountIdEntity setRegNo(String regNo) {
        this.regNo = regNo;
        return this;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public BankdataAccountIdEntity setAccountNo(String accountNo) {
        this.accountNo = accountNo;
        return this;
    }

    public String getAccountName() {
        return accountName;
    }

    public BankdataAccountIdEntity setAccountName(String accountName) {
        this.accountName = accountName;
        return this;
    }
}
