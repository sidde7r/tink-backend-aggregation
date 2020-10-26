package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator;

import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration.RabobankConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RabobankAuthenticator implements OAuth2Authenticator {

    private final RabobankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final RabobankConfiguration configuration;
    private final String redirectUrl;

    public RabobankAuthenticator(
            final RabobankApiClient apiClient,
            final PersistentStorage persistentStorage,
            final AgentConfiguration<RabobankConfiguration> agentConfiguration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    private RabobankConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    private String getRedirectUrl() {
        return Optional.ofNullable(redirectUrl)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public URL buildAuthorizeUrl(final String state) {
        final String clientId = getConfiguration().getClientId();

        final Form params =
                Form.builder()
                        .encodeSpacesWithPercent()
                        .put(QueryParams.RESPONSE_TYPE, QueryValues.CODE)
                        .put(QueryParams.REDIRECT_URI, getRedirectUrl())
                        .put(QueryParams.CLIENT_ID, clientId)
                        .put(QueryParams.SCOPE, QueryValues.SCOPES)
                        .put(QueryParams.STATE, state)
                        .build();

        return configuration.getUrls().getAuthorizeUrl().concat("?" + params.serialize());
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(final String code) {

        final Form request =
                Form.builder()
                        .put(QueryParams.GRANT_TYPE, QueryValues.AUTHORIZATION_CODE)
                        .put(QueryParams.CODE, code)
                        .put(QueryParams.REDIRECT_URI, getRedirectUrl())
                        .build();

        final TokenResponse tokenResponse = apiClient.exchangeAuthorizationCode(request);
        final String consentId = getConsentId(tokenResponse.getScope());
        persistentStorage.put(StorageKey.CONSENT_ID, consentId);

        return tokenResponse.toOauthToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(final String refreshToken)
            throws SessionException, BankServiceException {

        final Form request =
                Form.builder()
                        .put(QueryParams.GRANT_TYPE, QueryValues.REFRESH_TOKEN)
                        .put(QueryParams.REDIRECT_URI, getRedirectUrl())
                        .put(QueryParams.REFRESH_TOKEN, refreshToken)
                        .build();

        try {
            return apiClient.refreshAccessToken(request).toOauthToken();
        } catch (final HttpResponseException exception) {
            throw SessionError.SESSION_EXPIRED.exception(exception);
        }
    }

    @Override
    public void useAccessToken(final OAuth2Token accessToken) {}

    private String getConsentId(String scope) {
        String consentId = null;
        final String[] words = scope.split(" ");
        for (String word : words) {
            if (word.contains("_")) {
                String parts[] = word.split("_");
                if (isUuid(parts[0])) {
                    consentId = parts[0];
                    break;
                }
            }
        }
        if (Strings.isNullOrEmpty(consentId)) {
            throw BankServiceError.CONSENT_INVALID.exception("Missing consent ID.");
        }
        return consentId;
    }

    static boolean isUuid(String uuid) {
        return uuid.matches(RabobankConstants.UUID_PATTERN);
    }
}
