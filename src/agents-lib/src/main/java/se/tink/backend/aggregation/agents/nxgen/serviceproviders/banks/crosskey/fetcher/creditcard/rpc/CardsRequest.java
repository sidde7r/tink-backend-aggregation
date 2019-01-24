package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.creditcard.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardsRequest {
    private final String deviceId;

    public CardsRequest(String deviceId) {
        this.deviceId = deviceId;
    }
}
