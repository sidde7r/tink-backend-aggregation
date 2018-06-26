package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankTransactionCode {
    private String domain;
    private String code;
    private String subCode;

    public String getDomain() {
        return domain;
    }

    public String getCode() {
        return code;
    }

    public String getSubCode() {
        return subCode;
    }
}
