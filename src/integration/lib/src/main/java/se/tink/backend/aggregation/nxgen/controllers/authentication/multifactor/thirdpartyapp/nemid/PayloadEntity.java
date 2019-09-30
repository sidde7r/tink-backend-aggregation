package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PayloadEntity {
    private String response;
    private String signedResponse;

    public String getResponse() {
        return response;
    }

    public String getSignedResponse() {
        return signedResponse;
    }
}
