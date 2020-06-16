package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class RedsysConfiguration implements ClientConfiguration {

    @Secret private String baseAuthUrl;
    @Secret private String baseAPIUrl;
    @Secret private String clientId;
    @AgentConfigParam private String redirectUrl;
    @Secret private List<String> scopes;
    private String clientSigningKeyPath;
    private String clientSigningKeyPassword;
    @Secret private String clientSigningCertificate;

    public String getBaseAuthUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseAuthUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base Auth URL"));

        return baseAuthUrl;
    }

    public String getBaseAPIUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseAPIUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base API URL"));

        return baseAPIUrl;
    }

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

    public Optional<String> getClientSigningKeyPath() {
        return Optional.ofNullable(clientSigningKeyPath);
    }

    public Optional<String> getClientSigningKeyPassword() {
        return Optional.ofNullable(clientSigningKeyPassword);
    }

    public String getClientSigningCertificate() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningCertificate),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Signing Certificate"));
        return clientSigningCertificate;
    }

    public List<String> getScopes() {
        Preconditions.checkNotNull(
                scopes, String.format(ErrorMessages.INVALID_CONFIGURATION, "scopes"));
        Preconditions.checkArgument(
                !Iterables.isEmpty(scopes),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "scopes"));
        return scopes;
    }
}
