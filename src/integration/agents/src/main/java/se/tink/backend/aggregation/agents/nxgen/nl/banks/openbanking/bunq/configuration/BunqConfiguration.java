package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseConfiguration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class BunqConfiguration extends BunqBaseConfiguration implements ClientConfiguration {

    @JsonProperty private String redirectUrl;

    @JsonProperty private String psd2ApiKey;

    @JsonProperty private String clientId;

    @JsonProperty private String clientSecret;

    @JsonProperty private String psd2InstallationKeyPair;

    @JsonProperty private String psd2ClientAuthToken;

    public String getPsd2InstallationKeyPair() {
        return psd2InstallationKeyPair;
    }

    public String getPsd2ClientAuthToken() {
        return psd2ClientAuthToken;
    }

    public String getPsd2ApiKey() {
        return psd2ApiKey;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
