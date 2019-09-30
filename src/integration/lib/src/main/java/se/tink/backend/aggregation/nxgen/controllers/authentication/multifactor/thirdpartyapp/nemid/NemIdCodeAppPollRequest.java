package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdCodeAppPollRequest {
    private String ticket;

    public NemIdCodeAppPollRequest(String ticket) {
        this.ticket = ticket;
    }
}
