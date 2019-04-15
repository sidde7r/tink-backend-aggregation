package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PollCodeAppResponse {
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
