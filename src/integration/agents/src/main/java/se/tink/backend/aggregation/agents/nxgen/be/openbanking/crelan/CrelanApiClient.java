package se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.CrelanConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.CrelanConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.CrelanConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.CrelanConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.CrelanConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.CrelanConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.CrelanConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.authenticator.rpc.PostConsentBody;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.authenticator.rpc.PostConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.configuration.CrelanConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.fetcher.transactionalaccount.rpc.GetBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class CrelanApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private CrelanConfiguration configuration;

    public CrelanApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    private CrelanConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(CrelanConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromStorage();

        return createRequest(url).header(HeaderKeys.AUTHORIZATION, authToken.getAccessToken());
    }

    private RequestBuilder createFetchingRequest(URL url) {
        return createRequestInSession(url).header(HeaderKeys.CONSENT_ID, getConsentIdFromStorage());
    }

    private String getConsentIdFromStorage() {
        return persistentStorage.get(StorageKeys.CONSENT_ID);
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public PostConsentResponse createConsent(PostConsentBody postConsentBody) {
        return createRequest(Urls.POST_CONSENT)
                .header(HeaderKeys.TPP_REDIRECT_URI, configuration.getRedirectUrl())
                .header(HeaderKeys.PSU_IP_ADDRESS, QueryValues.PSU_IP_ADDRESS)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .body(postConsentBody)
                .post(PostConsentResponse.class);
    }

    public URL buildAuthorizeUrl(String state) {
        return Urls.AUTHORIZE
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.REDIRECT_URI, configuration.getRedirectUrl())
                .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId())
                .queryParam(QueryKeys.SCOPE, "AIS:" + getConsentIdFromStorage())
                .queryParam(QueryKeys.CODE_CHALLENGE, QueryValues.CODE_CHALLENGE)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                .queryParam(QueryKeys.CODE_CHALLENGE_TYPE, QueryValues.CODE_CHALLENGE_TYPE);
    }

    public GetTokenResponse getToken(GetTokenForm getTokenForm) {
        return createRequest(Urls.TOKEN)
                .body(getTokenForm, MediaType.APPLICATION_FORM_URLENCODED)
                .post(GetTokenResponse.class);
    }

    public GetAccountsResponse getAccounts() {
        return createFetchingRequest(Urls.GET_ACCOUNTS).get(GetAccountsResponse.class);
    }

    public GetBalanceResponse getBalance(AccountEntity account) {
        return createFetchingRequest(Urls.GET_BALANCE.parameter(IdTags.ACCOUNT_ID, account.getId()))
                .get(GetBalanceResponse.class);
    }

    public GetTransactionsResponse getTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return createFetchingRequest(
                        Urls.GET_TRANSACTIONS.parameter(
                                IdTags.ACCOUNT_ID, account.getApiIdentifier()))
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .get(GetTransactionsResponse.class);
    }
}
