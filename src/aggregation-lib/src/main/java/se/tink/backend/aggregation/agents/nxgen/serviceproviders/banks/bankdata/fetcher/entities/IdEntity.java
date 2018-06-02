package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdEntity {
    private int regNo;
    private int accountNo;

    public int getRegNo() {
        return regNo;
    }

    public int getAccountNo() {
        return accountNo;
    }
}
