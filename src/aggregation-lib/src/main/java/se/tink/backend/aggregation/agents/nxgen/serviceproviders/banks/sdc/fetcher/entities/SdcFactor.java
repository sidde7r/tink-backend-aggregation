package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SdcFactor {
    private double factor;
    private String localized;
    private String localizedWithPercent;

    public double getFactor() {
        return factor;
    }

    public String getLocalized() {
        return localized;
    }

    public String getLocalizedWithPercent() {
        return localizedWithPercent;
    }
}
