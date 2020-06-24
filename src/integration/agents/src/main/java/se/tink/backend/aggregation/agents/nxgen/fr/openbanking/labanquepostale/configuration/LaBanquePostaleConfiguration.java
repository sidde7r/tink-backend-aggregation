package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.configuration.BerlinGroupConfiguration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
public class LaBanquePostaleConfiguration implements BerlinGroupConfiguration {

    @JsonProperty @Secret private String oauthBaseUrl;
    @JsonProperty @Secret private String baseUrl;
    @JsonProperty @Secret @ClientIdConfiguration private String clientId;
    @JsonProperty @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;
    @JsonProperty @Secret private String psuIpAddress;

    @Override
    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    @Override
    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSecret),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Secret"));

        return clientSecret;
    }

    @Override
    public String getBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base URL"));

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

    public String getOauthBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(oauthBaseUrl),
                String.format(
                        LaBanquePostaleConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "oauth base url"));

        return oauthBaseUrl;
    }
}
