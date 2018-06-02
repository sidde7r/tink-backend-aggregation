package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecurityCodeRequest {

    private SecurityCode securityCode;

    public static SecurityCodeRequest create(String code) {
        return new SecurityCodeRequest()
                .setSecurityCode(
                        SecurityCode.create(code)
                );
    }

    private SecurityCodeRequest setSecurityCode(
            SecurityCode securityCode) {
        this.securityCode = securityCode;
        return this;
    }
}
