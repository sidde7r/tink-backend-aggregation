package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TargetUrlResponse {
    private String targetUrl;

    public String getTargetUrl() {
        return targetUrl;
    }
}
