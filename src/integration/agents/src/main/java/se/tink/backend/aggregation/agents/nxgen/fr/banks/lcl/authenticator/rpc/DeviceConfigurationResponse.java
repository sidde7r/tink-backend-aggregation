package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DeviceConfigurationResponse {
    private boolean response;

    public boolean isResponse() {
        return response;
    }
}
