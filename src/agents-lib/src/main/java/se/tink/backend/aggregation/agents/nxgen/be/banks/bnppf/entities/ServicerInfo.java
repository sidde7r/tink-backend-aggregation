package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ServicerInfo {
    private String schemeName;
    private String identification;
    private String bankName;

    public String getSchemeName() {
        return schemeName;
    }

    public String getIdentification() {
        return identification;
    }

    public String getBankName() {
        return bankName;
    }
}
