package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SdcCustodyDetailsAccount {

    private String id;

    public String getId() {
        return id;
    }
}
