package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.rpc.device;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.entities.SecurityCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.encryption.LibTFA;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EncryptedSecurityCodeRequest {

    private String encSecurityCode;

    private EncryptedSecurityCodeRequest() {}

    public static EncryptedSecurityCodeRequest create(
            SecurityCodeRequest securityCode, LibTFA tfa) {
        return new EncryptedSecurityCodeRequest()
                .setEncSecurityCode(tfa.encryptAndEncodeBase64(securityCode));
    }

    private EncryptedSecurityCodeRequest setEncSecurityCode(String encSecurityCode) {
        this.encSecurityCode = encSecurityCode;
        return this;
    }
}
