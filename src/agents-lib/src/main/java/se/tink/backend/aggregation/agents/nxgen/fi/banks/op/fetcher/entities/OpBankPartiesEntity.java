package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpBankPartiesEntity {
    private String name;
    private String roleCode;

    public String getName() {
        return name;
    }

    public String getRoleCode() {
        return roleCode;
    }
}
