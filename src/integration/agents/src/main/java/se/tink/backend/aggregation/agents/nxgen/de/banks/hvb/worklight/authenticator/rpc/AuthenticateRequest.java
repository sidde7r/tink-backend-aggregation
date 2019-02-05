package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.security.interfaces.RSAPrivateKey;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.AuthenticateJwt;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.CertManager;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.Jwt;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities.WlDeviceAutoProvisioningRealmRequestEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class AuthenticateRequest {

    @JsonProperty("wl_deviceAutoProvisioningRealm")
    private WlDeviceAutoProvisioningRealmRequestEntity wlDeviceAutoProvisioningRealm;

    public AuthenticateRequest(final String token, final String certificate, final RSAPrivateKey privateKey,
            final String moduleName) {
        wlDeviceAutoProvisioningRealm = new WlDeviceAutoProvisioningRealmRequestEntity();
        wlDeviceAutoProvisioningRealm.setID(getId(token, certificate, privateKey, moduleName));
    }

    private String getId(final String token, final String certificate, final RSAPrivateKey privateKey,
            final String moduleName) {
        final Jwt jwt = new AuthenticateJwt(token, certificate, moduleName);
        return CertManager.createJwt(jwt, privateKey);
    }
}

