package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.authenticator.rpc;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.AuthenticationKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.LoginParameter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EnrollmentRequest {
    private final boolean hasScreenLock;
    private final boolean jailbreak;
    private final Integer screenHeight;
    private final Integer screenWidth;
    private final String appName;
    private final String deviceName;
    private final String deviceId;
    private final String osName;
    private final String language;
    private final String osVersion;
    private final String appVersion;
    private final String pushToken;
    private final String user;
    private final String password;

    public EnrollmentRequest(Credentials credentials) {
        this.user = credentials.getField(AuthenticationKeys.USERNAME);
        this.password = credentials.getField(AuthenticationKeys.PASSWORD);
        this.hasScreenLock = true;
        this.jailbreak = false;
        this.screenHeight = LoginParameter.SCREEN_HEIGHT;
        this.screenWidth = LoginParameter.SCREEN_WIDTH;
        this.appName = LoginParameter.APP_NAME;
        this.deviceName = LoginParameter.DEVICE_NAME;
        this.deviceId = LoginParameter.DEVICE_ID;
        this.osName = LoginParameter.OS_NAME;
        this.language = LoginParameter.LANGUAGE;
        this.osVersion = LoginParameter.OS_VERSION;
        this.appVersion = LoginParameter.APP_VERSION;
        this.pushToken = LoginParameter.PUSH_TOKEN;
    }

    public String getPushToken() {
        return pushToken;
    }
}
