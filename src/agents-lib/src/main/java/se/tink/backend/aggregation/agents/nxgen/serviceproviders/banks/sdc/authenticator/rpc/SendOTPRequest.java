package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SendOTPRequest {
    private String transId;

    public SendOTPRequest setTransId(String transId) {
        this.transId = transId;
        return this;
    }
}
