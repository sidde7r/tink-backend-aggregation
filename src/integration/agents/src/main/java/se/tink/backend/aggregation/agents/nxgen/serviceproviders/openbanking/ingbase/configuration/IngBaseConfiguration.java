package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class IngBaseConfiguration implements ClientConfiguration {

    @JsonProperty @Secret private String baseUrl;
    @JsonProperty @Secret private String clientCertificate;
    @JsonProperty @Secret private String redirectUrl;

    public String getBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base URL"));
        return baseUrl;
    }

    public String getClientCertificate() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client certificate"));
        return clientCertificate;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Redirect URL"));
        return redirectUrl;
    }
}
