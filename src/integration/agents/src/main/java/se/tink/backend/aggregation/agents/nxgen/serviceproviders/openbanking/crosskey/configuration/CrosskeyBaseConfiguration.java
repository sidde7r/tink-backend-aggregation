package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class CrosskeyBaseConfiguration implements ClientConfiguration {

    @JsonProperty @Secret private String clientId;
    @JsonProperty @AgentConfigParam private String redirectUrl;
    @JsonProperty private List<String> scopes;
    @JsonProperty @SensitiveSecret private String clientSecret;
    @JsonProperty @AgentConfigParam private String xFapiFinancialId;

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Redirect URL"));

        return redirectUrl;
    }

    public List<String> getScopes() {
        Preconditions.checkNotNull(
                scopes, String.format(ErrorMessages.INVALID_CONFIGURATION, "scopes"));
        Preconditions.checkArgument(
                !Iterables.isEmpty(scopes),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "scopes"));

        return scopes;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientSecret;
    }

    public String getxFapiFinancialId() {
        Preconditions.checkNotNull(
            Strings.emptyToNull(xFapiFinancialId),
            String.format(ErrorMessages.INVALID_CONFIGURATION, "xFapiFinancialId"));

        return xFapiFinancialId;
    }
}
