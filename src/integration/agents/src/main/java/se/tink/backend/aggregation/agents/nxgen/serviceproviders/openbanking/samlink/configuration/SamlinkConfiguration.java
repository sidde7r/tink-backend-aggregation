package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.configuration.BerlinGroupConfiguration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
public class SamlinkConfiguration implements BerlinGroupConfiguration {
    @JsonProperty @Secret private String oauthBaseUrl;
    @JsonProperty @Secret private String baseUrl;
    @JsonProperty @Secret @ClientIdConfiguration private String clientId;
    @JsonProperty @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;
    @JsonProperty @Secret private String psuIpAddress;
    @Secret private String keyId;
    @Secret private String certificate;
    @Secret private String apiKey;
    @Secret private String redirectUrl;

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getKeyId() {
        return keyId;
    }

    public String getCertificate() {
        return certificate;
    }

    public String getOauthBaseUrl() {
        return oauthBaseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public String getPsuIpAddress() {
        if (Objects.nonNull(psuIpAddress)) {
            return psuIpAddress;
        }

        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return BerlinGroupConstants.DEFAULT_IP;
        }
    }
}
