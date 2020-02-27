package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator;

import java.security.KeyPair;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class AuthenticationData {

    private String deviceId;
    private KeyPair keyPair;
    private JwkHeader jwkHeader;
    private String clientId;
    private String userId;
    private String pin;
    private String code;
    private String applicationSessionId;
    private Instant instant;

    static AuthenticationData forRegistration(
            String deviceId, KeyPair keyPair, JwkHeader jwkHeader) {
        return new AuthenticationData()
                .setDeviceId(deviceId)
                .setKeyPair(keyPair)
                .setJwkHeader(jwkHeader);
    }

    static AuthenticationData forAuthorization(
            String clientId,
            String username,
            String pin,
            KeyPair keyPair,
            JwkHeader jwkHeader,
            String applicationSessionId) {
        return new AuthenticationData()
                .setClientId(clientId)
                .setPin(pin)
                .setUserId(username)
                .setKeyPair(keyPair)
                .setJwkHeader(jwkHeader)
                .setApplicationSessionId(applicationSessionId);
    }
}
