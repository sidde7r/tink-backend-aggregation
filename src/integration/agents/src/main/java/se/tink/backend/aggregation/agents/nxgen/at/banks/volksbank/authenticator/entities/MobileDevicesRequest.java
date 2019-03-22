package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MobileDevicesRequest {
    @JsonProperty("geraeteId")
    private String setupId;

    @JsonProperty("geraeteName")
    private String setupName;

    @JsonProperty("geraeteOs")
    private String setupOs;

    private String pushToken;

    public String getSetupId() {
        return setupId;
    }

    public void setSetupId(String setupId) {
        this.setupId = setupId;
    }

    public String getSetupName() {
        return setupName;
    }

    public void setSetupName(String setupName) {
        this.setupName = setupName;
    }

    public String getSetupOs() {
        return setupOs;
    }

    public void setSetupOs(String setupOs) {
        this.setupOs = setupOs;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }
}
