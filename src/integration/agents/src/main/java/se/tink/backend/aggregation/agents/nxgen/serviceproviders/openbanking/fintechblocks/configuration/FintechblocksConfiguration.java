package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class FintechblocksConfiguration implements ClientConfiguration {

    private String clientId;
    private String redirectUri;
    private String clientSigningKeyPath;
    private String baseUrl;

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(
                        BnpParibasFortisConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "Client ID"));

        return clientId;
    }

    public String getRedirectUri() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUri),
                String.format(
                        BnpParibasFortisConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "Redirect URL"));

        return redirectUri;
    }

    public String getClientSigningKeyPath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningKeyPath),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Signing key path"));

        return clientSigningKeyPath;
    }

    public String getBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base Url"));

        return baseUrl;
    }
}
