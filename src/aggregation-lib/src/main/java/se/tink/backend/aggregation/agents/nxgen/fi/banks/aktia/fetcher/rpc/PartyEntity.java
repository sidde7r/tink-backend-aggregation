package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PartyEntity {
    private String name;
    private String customerTypeCode;
    private String roleCode;

    public String getName() {
        return name;
    }

    public String getCustomerTypeCode() {
        return customerTypeCode;
    }

    public String getRoleCode() {
        return roleCode;
    }
}
