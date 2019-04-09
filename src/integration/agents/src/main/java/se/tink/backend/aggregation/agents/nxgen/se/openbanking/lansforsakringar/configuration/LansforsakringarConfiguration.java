package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class LansforsakringarConfiguration implements ClientConfiguration {
    private String clientId;
    private String clientSecret;
    private String consentId;

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSecret),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Secret"));

        return clientSecret;
    }

    public String getConsentId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(consentId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Consent ID"));

        return consentId;
    }
}
