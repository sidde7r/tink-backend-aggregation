package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class FintechblocksConfiguration implements ClientConfiguration {

    @Secret private String clientId;
    @Secret private String redirectUrl;
    @Secret private String clientSigningKeyPath;
    @Secret private String baseUrl;

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(
                        BnpParibasFortisConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "Client ID"));

        return clientId;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(
                        BnpParibasFortisConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "Redirect URL"));

        return redirectUrl;
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
