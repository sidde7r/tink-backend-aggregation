package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClientInfo {
    private String clientId;
    private String clientSecret;
    private String RegistrationId;
    private Date issuedAt;
    private Date expiresAt;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRegistrationId() {
        return RegistrationId;
    }

    public Date getIssuedAt() {
        return issuedAt;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }
}
