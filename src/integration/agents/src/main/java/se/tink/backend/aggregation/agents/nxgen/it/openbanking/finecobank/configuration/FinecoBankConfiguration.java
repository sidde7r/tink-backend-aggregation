package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class FinecoBankConfiguration implements ClientConfiguration {

    private String clientId;
    private String redirectUrl;
<<<<<<< HEAD
    private String certificateId;
=======
>>>>>>> refactor(FinecoBank): Refctoring test, fetch multiple and payment controller
    private String psuIpAddress;

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

<<<<<<< HEAD
    public String getCertificateId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(certificateId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Certificate ID"));
        return certificateId;
    }

    public String getPsuIpAddress() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(psuIpAddress),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "PSU IP Address"));
=======
    public String getPsuIpAddress() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(psuIpAddress),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Psu ip adress"));

>>>>>>> refactor(FinecoBank): Refctoring test, fetch multiple and payment controller
        return psuIpAddress;
    }
}
