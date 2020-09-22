package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.IspConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CheckRecordedDeviceRequestPayload {

    private String abi = "01025";
    private String clientId = IspConstants.Crypto.CLIENT_ID;
    private String clientSecret = IspConstants.Crypto.CLIENT_SECRET;
    private boolean notCreateSession = false;
    private String deviceId;

    @JsonProperty("ricordami")
    private String rememberMeCode;

    public CheckRecordedDeviceRequestPayload(String deviceId, String rememberMeCode) {
        this.deviceId = deviceId;
        this.rememberMeCode = rememberMeCode;
    }
}
