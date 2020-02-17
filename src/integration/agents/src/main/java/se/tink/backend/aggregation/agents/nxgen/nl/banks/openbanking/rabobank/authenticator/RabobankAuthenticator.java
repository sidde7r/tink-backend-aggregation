package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.Consents;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration.RabobankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.utils.RabobankUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RabobankAuthenticator implements OAuth2Authenticator {

    private static final Logger logger = LoggerFactory.getLogger(RabobankAuthenticator.class);

    private final RabobankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final RabobankConfiguration configuration;

    public RabobankAuthenticator(
            final RabobankApiClient apiClient,
            final PersistentStorage persistentStorage,
            final RabobankConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    private RabobankConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public URL buildAuthorizeUrl(final String state) {
        final String redirectUri = getConfiguration().getRedirectUrl();
        final String clientId = getConfiguration().getClientId();

        final Form params =
                Form.builder()
                        .encodeSpacesWithPercent()
                        .put(QueryParams.RESPONSE_TYPE, QueryValues.CODE)
                        .put(QueryParams.REDIRECT_URI, redirectUri)
                        .put(QueryParams.CLIENT_ID, clientId)
                        .put(QueryParams.SCOPE, QueryValues.SCOPES)
                        .put(QueryParams.STATE, state)
                        .build();

        return configuration.getUrls().getAuthorizeUrl().concat("?" + params.serialize());
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(final String code) {
        final String redirectUri = getConfiguration().getRedirectUrl();

        final Form request =
                Form.builder()
                        .put(QueryParams.GRANT_TYPE, QueryValues.AUTHORIZATION_CODE)
                        .put(QueryParams.CODE, code)
                        .put(QueryParams.REDIRECT_URI, redirectUri)
                        .build();

        final TokenResponse tokenResponse = apiClient.exchangeAuthorizationCode(request);
        final String consentId = getConsentId(tokenResponse.getScope());
        persistentStorage.put(StorageKey.CONSENT_ID, consentId);

        return tokenResponse.toOauthToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(final String refreshToken)
            throws SessionException, BankServiceException {
        logger.info("Got persist refresh token " + refreshToken);

        final String redirectUri = getConfiguration().getRedirectUrl();

        final Form request =
                Form.builder()
                        .put(QueryParams.GRANT_TYPE, QueryValues.REFRESH_TOKEN)
                        .put(QueryParams.REDIRECT_URI, redirectUri)
                        .put(QueryParams.REFRESH_TOKEN, refreshToken)
                        .build();

        try {
            return apiClient.refreshAccessToken(request).toOauthToken();
        } catch (final HttpResponseException exception) {
            // TODO Debug purpose
            if (exception.getResponse().getBody(String.class).contains("invalid_grant")) {
                final OAuth2Token oAuth2Token = RabobankUtils.getOauthToken(persistentStorage);
                logger.info(
                        "Invalid refresh token {}, Is token expire? {}",
                        oAuth2Token.getRefreshToken(),
                        oAuth2Token.hasRefreshExpire());
            }
            throw SessionError.SESSION_EXPIRED.exception(exception);
        }
    }

    @Override
    public void useAccessToken(final OAuth2Token accessToken) {}

    private String getConsentId(String scope) {
        String consentId = null;
        final String[] words = scope.split(" ");
        for (String word : words) {
            if (word.contains(Consents.PREFIX)) {
                String parts[] = word.split("_");
                consentId = parts[0];
                break;
            }
        }
        if (consentId.isEmpty()) {
            throw BankServiceError.CONSENT_INVALID.exception("Missing consent ID.");
        }
        return consentId;
    }
}
