package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdLoginResponse {
    private boolean initOk;
    private String loginUrl;

    public boolean isInitOk() {
        return initOk;
    }

    public String getLoginUrl() {
        return loginUrl;
    }
}
