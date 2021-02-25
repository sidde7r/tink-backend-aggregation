package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EnrollmentResponse {
    private String refreshToken;
    private String accessToken;
    private String language;
    private String branch;
    private String numDevices;
    private String maxDevices;
    private boolean maxDevicesAlertFlag;

    public String getAccessToken() {
        return accessToken;
    }
}
