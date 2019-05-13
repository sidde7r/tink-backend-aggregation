package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksConstants.ApiService;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.rpc.CreateConsentBody;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.rpc.CreateConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.configuration.FintechblocksConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class FintechblocksApiClient {

    protected final TinkHttpClient client;
    protected final PersistentStorage persistentStorage;
    protected FintechblocksConfiguration configuration;

    protected FintechblocksApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    protected FintechblocksConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(FintechblocksConfiguration configuration) {
        this.configuration = configuration;
    }

    protected RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    protected RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromStorage();

        return createRequest(url).addBearerToken(authToken);
    }

    protected OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public OAuth2Token authorize(GetTokenForm getTokenForm) {
        return createRequest(new URL(configuration.getBaseUrl() + ApiService.TOKEN))
                .body(getTokenForm, MediaType.APPLICATION_FORM_URLENCODED)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }

    public CreateConsentResponse createConsent(
            CreateConsentBody createConsentBody, OAuth2Token token, String jwt) {
        return createRequest(new URL(configuration.getBaseUrl() + ApiService.CREATE_CONSENT))
                .addBearerToken(token)
                .header(HeaderKeys.X_JWS_SIGNATURE, jwt)
                .post(CreateConsentResponse.class, createConsentBody);
    }

    public URL buildAuthorizeUrl(String state, String request) {
        return new URL(configuration.getBaseUrl() + ApiService.AUTH)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId())
                .queryParam(QueryKeys.REDIRECT_URI, configuration.getRedirectUri())
                .queryParam(QueryKeys.SCOPE, QueryValues.ACCOUNTS)
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.REQUEST, request);
    }

    public GetAccountsResponse getAccounts() {
        return createRequestInSession(new URL(configuration.getBaseUrl() + ApiService.GET_ACCOUNTS))
                .get(GetAccountsResponse.class);
    }

    public GetBalancesResponse getBalances(AccountEntity acc) {
        return createRequestInSession(
                        new URL(configuration.getBaseUrl() + ApiService.GET_BALANCES)
                                .parameter(IdTags.ACCOUNT_ID, acc.getAccountId()))
                .get(GetBalancesResponse.class);
    }

    public GetTransactionsResponse getTransactions(TransactionalAccount account, int page) {
        return createRequestInSession(
                        new URL(configuration.getBaseUrl() + ApiService.GET_TRANSACTIONS)
                                .parameter(IdTags.ACCOUNT_ID, account.getApiIdentifier()))
                .queryParam(QueryKeys.PAGE, String.valueOf(page))
                .get(GetTransactionsResponse.class);
    }
}
