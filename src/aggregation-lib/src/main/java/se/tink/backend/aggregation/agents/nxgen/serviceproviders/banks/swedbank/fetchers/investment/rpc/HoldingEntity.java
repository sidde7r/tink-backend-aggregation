package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HoldingEntity {
    private String name;
    private String tsid;

    public String getName() {
        return name;
    }

    public String getTsid() {
        return tsid;
    }
}
