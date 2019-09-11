package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class BelfiusConfiguration implements ClientConfiguration {

    private String clientId;
    private String redirectUrl;
    private String baseUrl;
    private String certificateId;
    private String clientSecret;
    private String eidasQwac;

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Redirect url"));

        return redirectUrl;
    }

    public String getBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base url"));

        return baseUrl;
    }

    public String getCertificateId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(certificateId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Certificate ID"));

        return certificateId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSecret),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Secret"));

        return clientSecret;
    }

    public String getEidasQwac() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(eidasQwac),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Eidas Qwac"));

        return eidasQwac;
    }
}
