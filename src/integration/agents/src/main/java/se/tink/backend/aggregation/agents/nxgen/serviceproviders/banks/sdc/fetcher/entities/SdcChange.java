package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SdcChange {
    private SdcAmount delta;
    private SdcFactor factor;

    public SdcAmount getDelta() {
        return delta;
    }

    public SdcFactor getFactor() {
        return factor;
    }
}
