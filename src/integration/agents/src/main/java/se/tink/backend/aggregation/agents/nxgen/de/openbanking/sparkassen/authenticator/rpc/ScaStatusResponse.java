package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaStatusResponse {

    private String scaStatus;

    public String getScaStatus() {
        return scaStatus;
    }
}
