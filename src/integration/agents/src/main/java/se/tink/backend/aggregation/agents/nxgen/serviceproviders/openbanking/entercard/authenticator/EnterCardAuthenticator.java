package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.authenticator;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.configuration.EnterCardConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class EnterCardAuthenticator implements OAuth2Authenticator {

    private final EnterCardApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final EnterCardConfiguration configuration;

    public EnterCardAuthenticator(
            EnterCardApiClient apiClient,
            PersistentStorage persistentStorage,
            EnterCardConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    private EnterCardConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return Urls.AUTHORIZATION
                .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId())
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.REDIRECT_URI, configuration.getRedirectUrl());
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code)
            throws BankServiceException, AuthenticationException {
        return apiClient.getToken(code);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        return null;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {}
}
