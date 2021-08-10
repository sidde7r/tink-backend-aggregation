package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Logs.LOG_TAG;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.PolishApiAccountClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.accounts.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.requests.common.PrivilegeListEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.responses.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.concreteagents.PolishApiLogicFlowConfigurator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiPersistentStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Slf4j
@RequiredArgsConstructor
public class PolishApiAuthenticator implements OAuth2Authenticator {

    private final PolishApiAuthorizationClient apiClient;
    private final PolishApiAccountClient accountApiClient;
    private final PolishApiPersistentStorage persistentStorage;
    private final PolishApiLogicFlowConfigurator authorizeRequestConfigurator;

    @Override
    public URL buildAuthorizeUrl(String state) {
        log.info("{} Authenticator - Building auth url", LOG_TAG);
        // if we need to authorize once again - we need to remove accounts data.
        persistentStorage.removeAccountsData();
        return apiClient.getAuthorizeUrl(state);
    }

    /**
     * Banks may have different flows here - according to the polish API standard flow should look
     * like: 1. Send first request (in build authorizeUrl on our end) - with ais-accounts scope. 2.
     * Fetch accounts 3. Exchange token from ais-accounts scope to ais.
     *
     * <p>But there are banks which do not support exchange token - in that case we should sent
     * first request with ais scope. In token response we will get account numbers which we should
     * persist in storage. With that account numbers we can fetch the details.
     *
     * <p>There is also case of mBank which supports exchange token flow, but has specific
     * identifiers for accounts so we still need to store some kind of identifiers from token
     * response.
     */
    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        log.info("{} Authenticator - Exchanging code for token", LOG_TAG);
        TokenResponse tokenResponse = apiClient.exchangeAuthorizationToken(code);
        OAuth2Token oAuth2Token = tokenResponse.toOauthToken();
        persistentStorage.persistToken(oAuth2Token);

        if (authorizeRequestConfigurator.shouldGetAccountListFromTokenResponse()) {
            persistentStorage.persistAccountIdentifiers(getAccountIdentifiers(tokenResponse));
        }

        if (authorizeRequestConfigurator.doesSupportExchangeToken()) {
            return exchangeToken();
        }

        return oAuth2Token;
    }

    private OAuth2Token exchangeToken() {
        try {
            persistentStorage.persistAccountIdentifiers(getAccountIdentifiers());
            log.info("{} Authenticator - Exchanging token for another token", LOG_TAG);
            TokenResponse exchangeTokenResponse =
                    apiClient.exchangeTokenForAis(persistentStorage.getToken().getAccessToken());
            // double persistent of account identifiers - that is caused by the fact that sometimes
            // accountNumber
            // must be changed to accountIdentifier.
            persistentStorage.persistAccountIdentifiers(
                    getAccountIdentifiers(exchangeTokenResponse));
            return exchangeTokenResponse.toOauthToken();
        } catch (RuntimeException e) {
            persistentStorage.removeAccountsData();
            persistentStorage.removeToken();
            throw e;
        }
    }

    private List<String> getAccountIdentifiers() {
        log.info("{} Authenticator - Fetching accounts", LOG_TAG);
        return accountApiClient.fetchAccounts().getAccounts().stream()
                .map(AccountsEntity::getAccountNumber)
                .collect(Collectors.toList());
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        log.info("{} Authenticator - Refreshing access token", LOG_TAG);
        TokenResponse tokenResponse = apiClient.exchangeRefreshToken(refreshToken);
        persistentStorage.persistAccountIdentifiers(getAccountIdentifiers(tokenResponse));
        return tokenResponse.toOauthToken();
    }

    private List<String> getAccountIdentifiers(TokenResponse tokenResponse) {
        return tokenResponse.getScopeDetails().getPrivilegeList().stream()
                .map(PrivilegeListEntity::getAccountIdentifier)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.persistToken(accessToken);
    }
}
