package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MobileDevicesRequest {
    private String geraeteId;
    private String geraeteName;
    private String geraeteOs;
    private String pushToken;

    public String getGeraeteId() {
        return geraeteId;
    }

    public void setGeraeteId(String geraeteId) {
        this.geraeteId = geraeteId;
    }

    public String getGeraeteName() {
        return geraeteName;
    }

    public void setGeraeteName(String geraeteName) {
        this.geraeteName = geraeteName;
    }

    public String getGeraeteOs() {
        return geraeteOs;
    }

    public void setGeraeteOs(String geraeteOs) {
        this.geraeteOs = geraeteOs;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }
}
