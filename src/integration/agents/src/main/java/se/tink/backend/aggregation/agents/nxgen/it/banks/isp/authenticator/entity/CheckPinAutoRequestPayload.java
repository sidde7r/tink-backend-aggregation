package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.IspConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CheckPinAutoRequestPayload {
    private String abi = "01025";

    @JsonProperty("bt")
    private String username;

    private String authType = "PIN";
    private String clientId = IspConstants.Crypto.CLIENT_ID;
    private String clientSecret = IspConstants.Crypto.CLIENT_SECRET;
    private String otp;

    public CheckPinAutoRequestPayload(String username, String otp) {
        this.username = username;
        this.otp = otp;
    }
}
