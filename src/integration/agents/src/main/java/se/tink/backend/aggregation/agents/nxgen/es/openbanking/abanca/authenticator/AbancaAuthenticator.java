package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.authenticator;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.configuration.AbancaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.rpc.entity.ErrorsEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.credentials.service.UserAvailability;

@Slf4j
public class AbancaAuthenticator implements OAuth2Authenticator {

    private final AbancaApiClient apiClient;
    private final AbancaConfiguration abancaConfiguration;
    private final UserAvailability userAvailability;

    public AbancaAuthenticator(
            AbancaApiClient apiClient,
            AbancaConfiguration configuration,
            UserAvailability userAvailability) {
        this.apiClient = apiClient;
        this.abancaConfiguration = configuration;
        this.userAvailability = userAvailability;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code)
            throws BankServiceException, AuthenticationException {
        final OAuth2Token token =
                apiClient.getToken(
                        TokenRequest.builder(
                                        abancaConfiguration.getClientId(),
                                        FormValues.GRANT_TYPE_CODE)
                                .setCode(code)
                                .build());

        // Trigger SCA now to prevent authentication errors during UPDATING
        abortIfUserNotAvailableForInteraction();
        triggerSCA(token);

        return token;
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        return apiClient.getToken(
                TokenRequest.builder(
                                abancaConfiguration.getClientId(), FormValues.GRANT_TYPE_REFRESH)
                        .setRefreshToken(refreshToken)
                        .build());
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        apiClient.setTokenToSession(accessToken);
    }

    private void abortIfUserNotAvailableForInteraction() {
        if (isUserUnavailableForInteraction()) {
            throw BankServiceError.SESSION_TERMINATED.exception(
                    "SCA request during auto refresh, aborting authentication");
        }
    }

    private boolean isUserUnavailableForInteraction() {
        return !userAvailability.isUserAvailableForInteraction();
    }

    private void triggerSCA(OAuth2Token token) {
        try {
            // request balances for the first account
            apiClient.setTokenToSession(token);
            apiClient.fetchAccounts().getData().stream()
                    .findFirst()
                    .ifPresent(account -> apiClient.fetchBalance(account.getId()));
        } catch (HttpResponseException hre) {
            final ErrorsEntity challengeError =
                    hre.getResponse()
                            .getBody(ErrorResponse.class)
                            .getChallengeError()
                            .orElseThrow(() -> hre);

            if (challengeError.isChallengeInvalid()) {
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
            }

            throw hre;
        }
    }
}
