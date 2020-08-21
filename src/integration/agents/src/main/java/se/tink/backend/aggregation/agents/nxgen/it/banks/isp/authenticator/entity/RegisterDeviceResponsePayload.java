package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterDeviceResponsePayload {

    @JsonProperty("otsId")
    private String otpId;

    @JsonProperty("trxid")
    private String transactionId; // transaction id which needs to be sent in next request

    public String getOtpId() {
        return otpId;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
