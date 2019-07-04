package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.configuration;

import java.util.Base64;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class AbnAmroConfiguration implements ClientConfiguration {

    private String clientId;

    private String apiKey;

    private String redirectUrl;

    private String clientSSLP12;

    private String clientSSLKeyPassword;

    public String getClientId() {
        return clientId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public byte[] getClientSSLP12Bytes() {
        return Base64.getDecoder().decode(clientSSLP12);
    }

    public String getClientSSLKeyPassword() {
        return clientSSLKeyPassword;
    }
}
