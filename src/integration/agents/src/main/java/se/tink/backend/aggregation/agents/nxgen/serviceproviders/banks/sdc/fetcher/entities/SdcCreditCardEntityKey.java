package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SdcCreditCardEntityKey {
    private String rifsIdfr;

    public String getRifsIdfr() {
        return rifsIdfr;
    }
}
