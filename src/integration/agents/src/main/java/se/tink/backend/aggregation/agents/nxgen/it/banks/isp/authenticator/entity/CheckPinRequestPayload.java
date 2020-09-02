package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.IspConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CheckPinRequestPayload {

    private String abi = "";

    @JsonProperty("bt")
    private String username;

    private String channel = "02";
    private String clientId = IspConstants.Crypto.CLIENT_ID;
    private String clientSecret = IspConstants.Crypto.CLIENT_SECRET;
    private String day = "";
    private String month = "";
    private String pin;
    private String year = "";

    public CheckPinRequestPayload(String username, String pin) {
        this.username = username;
        this.pin = pin;
    }
}
