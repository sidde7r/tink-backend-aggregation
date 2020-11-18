package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.Meta;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class GeneralPayload {
    private String appType;
    private String appVersion;
    private String deviceId;
    private String deviceType;
    private String locale;
    private String osVersion;
    private String screenSize;
    private String secondFactor;
    private String data;
    private String cics;
    private String userId;
    private String pincode;

    public GeneralPayload(
            String userId, String pincode, String deviceId, String scaOption, String userLocale) {
        this.appType = Meta.APP_TYPE;
        this.appVersion = Meta.APP_VERSION;
        this.deviceId = deviceId;
        this.data = "";
        this.cics = "";
        this.deviceType = Meta.DEVICE_TYPE;
        // this value affects the language of nemID notification on user's device
        this.locale = userLocale;
        this.osVersion = Meta.OS_VERSION;
        this.screenSize = Meta.SCREEN_SIZE;

        this.secondFactor = scaOption;
        this.userId = userId;
        this.pincode = pincode;
    }
}
