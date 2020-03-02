package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecuritiesEntity {
    private String securityText;
    private String securityType;

    @Override
    public String toString() {
        return "Security: " + securityText + ", Type: " + securityType;
    }
}
