package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.encryption.LibTFA;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitNewProfileRequest {

    private String appId;
    private String authTp;
    private String cnonce;
    private String deviceSecurityContextId;
    private String handshakeKey;
    private String profileTransformationTp;

    private InitNewProfileRequest() {}

    public static InitNewProfileRequest create(
            HandelsbankenConfiguration configuration, LibTFA tfa) {
        return new InitNewProfileRequest()
                .setAppId(configuration.getAppId())
                .setAuthTp(configuration.getAuthTp())
                .setCnonce(tfa.generateNewClientNonce())
                .setDeviceSecurityContextId(tfa.getDeviceSecurityContextId())
                .setHandshakeKey(tfa.generateHandshakeAndGetPublicKey())
                .setProfileTransformationTp("1");
    }

    private InitNewProfileRequest setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    private InitNewProfileRequest setAuthTp(String authTp) {
        this.authTp = authTp;
        return this;
    }

    private InitNewProfileRequest setCnonce(String cnonce) {
        this.cnonce = cnonce;
        return this;
    }

    private InitNewProfileRequest setDeviceSecurityContextId(String deviceSecurityContextId) {
        this.deviceSecurityContextId = deviceSecurityContextId;
        return this;
    }

    private InitNewProfileRequest setHandshakeKey(String handshakeKey) {
        this.handshakeKey = handshakeKey;
        return this;
    }

    private InitNewProfileRequest setProfileTransformationTp(String profileTransformationTp) {
        this.profileTransformationTp = profileTransformationTp;
        return this;
    }
}
