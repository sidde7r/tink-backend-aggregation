package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.bunq.authenticator;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.rabobank.RabobankConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.rabobank.configuration.RabobankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bunq.BunqBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bunq.BunqBaseConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BunqAuthenticator implements OAuth2Authenticator {
    private final BunqBaseApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final BunqBaseConfiguration configuration;

    @Override
    public URL buildAuthorizeUrl(String state) {
        return null;
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        return null;
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        return null;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {}

    private RabobankConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        RabobankConstants.ErrorMessages.MISSING_CONFIGURATION));
    }
}
