package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.LoginParameter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginRequest {
    private final String osVersion;
    private final String appVersion;
    private final String language;
    private final String deviceName;
    private final int screenWidth;
    private final int screenHeight;
    private final boolean jailbreak;
    private final boolean hasScreenLock;
    private final String password;

    public LoginRequest(String password) {
        this.password = password;
        this.osVersion = LoginParameter.OS_VERSION;
        this.appVersion = LoginParameter.APP_VERSION;
        this.language = LoginParameter.LANGUAGE;
        this.deviceName = LoginParameter.DEVICE_NAME;
        this.screenWidth = LoginParameter.SCREEN_WIDTH;
        this.screenHeight = LoginParameter.SCREEN_HEIGHT;
        this.jailbreak = false;
        this.hasScreenLock = true;
    }
}
