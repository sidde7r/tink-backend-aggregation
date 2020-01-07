package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.AccountIdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CreditAgricoleBaseApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private CreditAgricoleBaseConfiguration configuration;

    public CreditAgricoleBaseApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    public void setConfiguration(CreditAgricoleBaseConfiguration configuration) {
        this.configuration = Preconditions.checkNotNull(configuration);
    }

    private CreditAgricoleBaseConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        CreditAgricoleBaseConstants.ErrorMessages
                                                .MISSING_CONFIGURATION));
    }

    public TokenResponse getToken(final String code) {
        return TokenUtils.get(getConfiguration(), client, code);
    }

    public OAuth2Token refreshToken(final String refreshToken) {
        final TokenResponse tokenResponse =
                TokenUtils.refresh(getConfiguration(), client, refreshToken);
        final OAuth2Token oAuth2Token = tokenResponse.toTinkToken();
        setTokenToSession(oAuth2Token);
        return oAuth2Token;
    }

    public GetAccountsResponse getAccounts() {
        return AccountsUtils.get(persistentStorage, client, configuration);
    }

    public void putConsents(final List<AccountIdEntity> listOfNecessaryConstents) {
        ConsentsUtils.put(persistentStorage, client, listOfNecessaryConstents, configuration);
    }

    public GetTransactionsResponse getTransactions(final String id, final URL next) {
        return TransactionsUtils.get(id, next, persistentStorage, client, configuration);
    }

    public EndUserIdentityResponse getEndUserIdentity() {
        return FetchEndUserIdentityUtils.get(persistentStorage, client, configuration);
    }

    private void setTokenToSession(OAuth2Token token) {
        persistentStorage.put(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, token);
    }
}
