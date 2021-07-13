package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionResponse {
    private String ima;
    private String userType;
    private boolean existsUser;
    private ImaginSessionResponse resImagin;

    public String getIma() {
        return ima;
    }

    public String getUserType() {
        return userType;
    }

    public ImaginSessionResponse getResImagin() {
        return resImagin;
    }
}
