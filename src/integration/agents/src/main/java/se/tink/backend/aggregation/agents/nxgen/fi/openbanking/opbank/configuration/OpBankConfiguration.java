package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class OpBankConfiguration implements ClientConfiguration {

    private String clientId;
    private String clientSecret;
    private String redirectUrl;
    private String apiKey;

    private String eidasProxyBaseUrl;
    private String eidasQwac;
    private String eidasQsealc;

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Secret"));

        return clientSecret;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Redirect URL"));

        return redirectUrl;
    }

    public String getApiKey() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(apiKey),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "API key"));

        return apiKey;
    }

    public String getEidasProxyBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(eidasProxyBaseUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "eIDAS Proxy base URL"));

        return eidasProxyBaseUrl;
    }

    public String getEidasQwac() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(eidasQwac),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "eIDAS QWAC"));

        return eidasQwac;
    }

    public String getEidasQsealc() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(eidasQsealc),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "eIDAS QSealC"));

        return eidasQsealc;
    }
}
