package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdCodeAppPollResponse {
    private String ticket;
    private PayloadEntity payload;
    private boolean confirmation;
    private String status;
    private String codeAppSerialNumber;
    private String codeAppIP;

    public String getStatus() {
        return status;
    }

    public String getCodeAppSerialNumber() {
        return codeAppSerialNumber;
    }

    public String getCodeAppIP() {
        return codeAppIP;
    }

    public PayloadEntity getPayload() {
        return payload;
    }

    public boolean isConfirmation() {
        return confirmation;
    }
}
