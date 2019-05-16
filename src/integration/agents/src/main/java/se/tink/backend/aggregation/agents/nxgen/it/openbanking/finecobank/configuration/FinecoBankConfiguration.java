package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class FinecoBankConfiguration implements ClientConfiguration {

    private String clientId;
    private String redirectUrl;
    private String clientSigningKeyPath;
    private String clientSigningCertificatePath;

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

    public String getClientSigningKeyPath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningKeyPath),
                String.format(
                        FinecoBankConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "Client Signing Key Path"));

        return clientSigningKeyPath;
    }

    public String getClientSigningCertificatePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningCertificatePath),
                String.format(
                        FinecoBankConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "Client Signing Certificate Path"));

        return clientSigningCertificatePath;
    }
}
