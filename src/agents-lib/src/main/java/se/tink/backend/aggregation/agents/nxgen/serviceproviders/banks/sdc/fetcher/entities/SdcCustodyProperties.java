package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SdcCustodyProperties {
    private boolean closed;

    public boolean isClosed() {
        return closed;
    }
}
