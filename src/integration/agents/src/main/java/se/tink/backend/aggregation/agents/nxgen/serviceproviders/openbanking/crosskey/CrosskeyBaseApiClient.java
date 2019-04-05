package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey;

import java.util.Arrays;
import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.accessconsents.AccessConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.accessconsents.AccessConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.accessconsents.RequestDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.accessconsents.RiskEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject.IdTokenClaim;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject.JWTAuthPayload;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject.JWTHeader;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.rpc.InitialTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.transaction.TransactionTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskeyAccountBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskeyAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskeyTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.utils.DateUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.utils.JWTUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import tink.org.apache.http.HttpHeaders;

public class CrosskeyBaseApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public CrosskeyBaseApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    public URL getAuthorizeUrl(String state) {

        InitialTokenResponse clientCredentials = getInitialTokenResponse();
        AccessConsentResponse accessConsentResponse = createAccessConsent(clientCredentials);

        sessionStorage.put(CrosskeyBaseConstants.StorageKeys.CONSENT, accessConsentResponse);

        JWTHeader jwtHeader =
                new JWTHeader(
                        CrosskeyBaseConstants.OIDCValues.ALG, CrosskeyBaseConstants.OIDCValues.TYP);

        JWTAuthPayload jwtAuthPayload =
                new JWTAuthPayload(
                        CrosskeyBaseConstants.OIDCValues.SCOPE,
                        new IdTokenClaim(
                                CrosskeyBaseConstants.OIDCValues.TOKEN_ID_PREFIX
                                        + accessConsentResponse.getData().getConsentId(),
                                false),
                        persistentStorage.get(CrosskeyBaseConstants.StorageKeys.CLIENT_ID),
                        persistentStorage.get(CrosskeyBaseConstants.StorageKeys.REDIRECT_URI),
                        state,
                        state,
                        persistentStorage.get(CrosskeyBaseConstants.StorageKeys.CLIENT_ID));

        return client.request(
                        new URL(
                                persistentStorage.get(
                                                CrosskeyBaseConstants.StorageKeys.BASE_AUTH_URL)
                                        + CrosskeyBaseConstants.Urls.OAUTH))
                .header(
                        CrosskeyBaseConstants.HeaderKeys.X_API_KEY,
                        persistentStorage.get(CrosskeyBaseConstants.StorageKeys.CLIENT_SECRET))
                .queryParam(
                        CrosskeyBaseConstants.QueryKeys.REQUEST,
                        JWTUtils.constructOIDCRequestObject(
                                jwtHeader,
                                jwtAuthPayload,
                                persistentStorage.get(CrosskeyBaseConstants.StorageKeys.KEY_PATH),
                                CrosskeyBaseConstants.Encryption.KEY_ALGORITHM))
                .queryParam(
                        CrosskeyBaseConstants.QueryKeys.RESPONSE_TYPE,
                        CrosskeyBaseConstants.QueryValues.RESPONSE_TYPE)
                .queryParam(
                        CrosskeyBaseConstants.QueryKeys.CLIENT_ID,
                        persistentStorage.get(CrosskeyBaseConstants.StorageKeys.CLIENT_ID))
                .queryParam(
                        CrosskeyBaseConstants.QueryKeys.REDIRECT_URI,
                        persistentStorage.get(CrosskeyBaseConstants.StorageKeys.REDIRECT_URI))
                .queryParam(CrosskeyBaseConstants.QueryKeys.STATE, state)
                .queryParam(CrosskeyBaseConstants.QueryKeys.NONCE, state)
                .queryParam(
                        CrosskeyBaseConstants.QueryKeys.SCOPE,
                        CrosskeyBaseConstants.QueryValues.SCOPE_OPENID)
                .getUrl();
    }

    private InitialTokenResponse getInitialTokenResponse() {
        return client.request(
                        new URL(
                                persistentStorage.get(
                                                CrosskeyBaseConstants.StorageKeys.BASE_API_URL)
                                        + CrosskeyBaseConstants.Urls.TOKEN))
                .header(
                        CrosskeyBaseConstants.HeaderKeys.X_API_KEY,
                        persistentStorage.get(CrosskeyBaseConstants.StorageKeys.CLIENT_SECRET))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .queryParam(
                        CrosskeyBaseConstants.QueryKeys.GRANT_TYPE,
                        CrosskeyBaseConstants.QueryValues.CLIENT_CREDENTIALS)
                .queryParam(
                        CrosskeyBaseConstants.QueryKeys.SCOPE,
                        CrosskeyBaseConstants.QueryValues.SCOPE)
                .queryParam(
                        CrosskeyBaseConstants.QueryKeys.CLIENT_ID,
                        persistentStorage.get(CrosskeyBaseConstants.StorageKeys.CLIENT_ID))
                .post(InitialTokenResponse.class);
    }

    private AccessConsentResponse createAccessConsent(InitialTokenResponse clientCredentials) {
        AccessConsentRequest accessConsentRequest =
                new AccessConsentRequest(
                        new RequestDataEntity(
                                "",
                                Arrays.asList(CrosskeyBaseConstants.OIDCValues.CONSENT_PERMISSIONS),
                                "",
                                ""),
                        new RiskEntity());

        return client.request(
                        new URL(
                                persistentStorage.get(
                                                CrosskeyBaseConstants.StorageKeys.BASE_API_URL)
                                        + CrosskeyBaseConstants.Urls.ACCOUNT_ACCESS_CONSENTS))
                .header(
                        CrosskeyBaseConstants.HeaderKeys.AUTHORIZATION,
                        CrosskeyBaseConstants.HeaderValues.AUTHORIZATION_BEARER_PREFIX
                                + clientCredentials.getAccessToken())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(
                        CrosskeyBaseConstants.HeaderKeys.X_API_KEY,
                        persistentStorage.get(CrosskeyBaseConstants.StorageKeys.CLIENT_SECRET))
                .header(
                        CrosskeyBaseConstants.HeaderKeys.X_FAPI_FINANCIAL_ID,
                        persistentStorage.get(StorageKeys.X_FAPI_FINANCIAL_ID))
                .post(AccessConsentResponse.class, accessConsentRequest);
    }

    public OAuth2Token getToken(String code) {

        return client.request(
                        new URL(
                                persistentStorage.get(
                                                CrosskeyBaseConstants.StorageKeys.BASE_API_URL)
                                        + CrosskeyBaseConstants.Urls.TOKEN))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .queryParam(
                        CrosskeyBaseConstants.QueryKeys.GRANT_TYPE,
                        CrosskeyBaseConstants.QueryValues.AUTHORIZATION_CODE)
                .queryParam(
                        CrosskeyBaseConstants.QueryKeys.REDIRECT_URI,
                        persistentStorage.get(CrosskeyBaseConstants.StorageKeys.REDIRECT_URI))
                .queryParam(CrosskeyBaseConstants.QueryKeys.CODE, code)
                .queryParam(
                        CrosskeyBaseConstants.QueryKeys.CLIENT_ID,
                        persistentStorage.get(CrosskeyBaseConstants.StorageKeys.CLIENT_ID))
                .post(TokenResponse.class)
                .toTinkToken();
    }

    public CrosskeyAccountsResponse fetchAccounts() {
        return client.request(
                        new URL(
                                persistentStorage.get(
                                                CrosskeyBaseConstants.StorageKeys.BASE_API_URL)
                                        + CrosskeyBaseConstants.Urls.ACCOUNTS))
                .addBearerToken(getTokenFromSession())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(
                        CrosskeyBaseConstants.HeaderKeys.X_API_KEY,
                        persistentStorage.get(CrosskeyBaseConstants.StorageKeys.CLIENT_SECRET))
                .header(
                        CrosskeyBaseConstants.HeaderKeys.X_FAPI_FINANCIAL_ID,
                        persistentStorage.get(StorageKeys.X_FAPI_FINANCIAL_ID))
                .get(CrosskeyAccountsResponse.class);
    }

    public CrosskeyAccountBalancesResponse fetchAccountBalances(String accountId) {
        return client.request(
                        new URL(
                                        persistentStorage.get(
                                                        CrosskeyBaseConstants.StorageKeys
                                                                .BASE_API_URL)
                                                + CrosskeyBaseConstants.Urls.ACCOUNT_BALANCES)
                                .parameter(
                                        CrosskeyBaseConstants.UrlParameters.ACCOUNT_ID, accountId))
                .addBearerToken(getTokenFromSession())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(
                        CrosskeyBaseConstants.HeaderKeys.X_API_KEY,
                        persistentStorage.get(CrosskeyBaseConstants.StorageKeys.CLIENT_SECRET))
                .header(
                        CrosskeyBaseConstants.HeaderKeys.X_FAPI_FINANCIAL_ID,
                        persistentStorage.get(StorageKeys.X_FAPI_FINANCIAL_ID))
                .get(CrosskeyAccountBalancesResponse.class);
    }

    public PaginatorResponse fetchCreditCardTransactions(
            CreditCardAccount account, Date fromDate, Date toDate) {

        AccessConsentResponse accessConsentFromSession = getAccessConsentFromSession();

        fromDate =
                DateUtils.getRestrictedDate(
                        fromDate,
                        accessConsentFromSession.getData().getTransactionFromDateTime(),
                        Date::after);
        toDate =
                DateUtils.getRestrictedDate(
                        toDate,
                        accessConsentFromSession.getData().getTransactionToDateTime(),
                        Date::before);

        if (toDate.before(fromDate)) {
            return new CrosskeyTransactionsResponse();
        }

        // TODO Use apiIdentifier once framework is updated
        return getTransactionsResponse(account.getBankIdentifier(), fromDate, toDate)
                .setTransactionType(TransactionTypeEntity.CREDIT);
    }

    public PaginatorResponse fetchTransactionalAccountTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {

        AccessConsentResponse accessConsentFromSession = getAccessConsentFromSession();

        fromDate =
                DateUtils.getRestrictedDate(
                        fromDate,
                        accessConsentFromSession.getData().getTransactionFromDateTime(),
                        Date::after);
        toDate =
                DateUtils.getRestrictedDate(
                        toDate,
                        accessConsentFromSession.getData().getTransactionToDateTime(),
                        Date::before);

        if (toDate.before(fromDate)) {
            return new CrosskeyTransactionsResponse();
        }

        return getTransactionsResponse(account.getApiIdentifier(), fromDate, toDate)
                .setTransactionType(TransactionTypeEntity.DEBIT);
    }

    private CrosskeyTransactionsResponse getTransactionsResponse(
            String apiIdentifier, Date fromDate, Date toDate) {
        return client.request(
                        new URL(
                                        persistentStorage.get(
                                                        CrosskeyBaseConstants.StorageKeys
                                                                .BASE_API_URL)
                                                + CrosskeyBaseConstants.Urls.ACCOUNT_TRANSACTIONS)
                                .parameter(
                                        CrosskeyBaseConstants.UrlParameters.ACCOUNT_ID,
                                        apiIdentifier))
                .addBearerToken(getTokenFromSession())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(
                        CrosskeyBaseConstants.HeaderKeys.X_API_KEY,
                        persistentStorage.get(CrosskeyBaseConstants.StorageKeys.CLIENT_SECRET))
                .header(
                        CrosskeyBaseConstants.HeaderKeys.X_FAPI_FINANCIAL_ID,
                        persistentStorage.get(StorageKeys.X_FAPI_FINANCIAL_ID))
                .queryParam(
                        CrosskeyBaseConstants.QueryKeys.FROM_BOOKING_DATE_TIME,
                        DateUtils.formatDateTime(
                                fromDate,
                                CrosskeyBaseConstants.Format.TIMESTAMP,
                                CrosskeyBaseConstants.Format.TIMEZONE))
                .queryParam(
                        CrosskeyBaseConstants.QueryKeys.TO_BOOKING_DATE_TIME,
                        DateUtils.formatDateTime(
                                toDate,
                                CrosskeyBaseConstants.Format.TIMESTAMP,
                                CrosskeyBaseConstants.Format.TIMEZONE))
                .get(CrosskeyTransactionsResponse.class);
    }

    private AccessConsentResponse getConsent(
            InitialTokenResponse clientCredentials, String consentId) {

        return client.request(
                        new URL(
                                        persistentStorage.get(
                                                        CrosskeyBaseConstants.StorageKeys
                                                                .BASE_API_URL)
                                                + CrosskeyBaseConstants.Urls.ACCOUNT_ACCESS_CONSENT)
                                .parameter(
                                        CrosskeyBaseConstants.UrlParameters.CONSENT_ID, consentId))
                .header(
                        CrosskeyBaseConstants.HeaderKeys.AUTHORIZATION,
                        CrosskeyBaseConstants.HeaderValues.AUTHORIZATION_BEARER_PREFIX
                                + clientCredentials.getAccessToken())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(
                        CrosskeyBaseConstants.HeaderKeys.X_API_KEY,
                        persistentStorage.get(CrosskeyBaseConstants.StorageKeys.CLIENT_SECRET))
                .header(
                        CrosskeyBaseConstants.HeaderKeys.X_FAPI_FINANCIAL_ID,
                        persistentStorage.get(StorageKeys.X_FAPI_FINANCIAL_ID))
                .get(AccessConsentResponse.class);
    }

    public OAuth2Token getRefreshToken(String refreshToken) {
        return client.request(
                        new URL(
                                persistentStorage.get(
                                                CrosskeyBaseConstants.StorageKeys.BASE_API_URL)
                                        + CrosskeyBaseConstants.Urls.TOKEN))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .queryParam(
                        CrosskeyBaseConstants.QueryKeys.GRANT_TYPE,
                        CrosskeyBaseConstants.QueryValues.REFRESH_TOKEN)
                .queryParam(CrosskeyBaseConstants.QueryKeys.REFRESH_TOKEN, refreshToken)
                .queryParam(
                        CrosskeyBaseConstants.QueryKeys.REDIRECT_URI,
                        persistentStorage.get(CrosskeyBaseConstants.StorageKeys.REDIRECT_URI))
                .queryParam(
                        CrosskeyBaseConstants.QueryKeys.CLIENT_ID,
                        persistentStorage.get(CrosskeyBaseConstants.StorageKeys.CLIENT_ID))
                .post(TokenResponse.class)
                .toTinkToken();
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(CrosskeyBaseConstants.StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        CrosskeyBaseConstants.Exceptions.MISSING_TOKEN));
    }

    public void setTokenToSession(OAuth2Token accessToken) {
        sessionStorage.put(CrosskeyBaseConstants.StorageKeys.TOKEN, accessToken);
    }

    private AccessConsentResponse getAccessConsentFromSession() {
        return sessionStorage
                .get(CrosskeyBaseConstants.StorageKeys.CONSENT, AccessConsentResponse.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        CrosskeyBaseConstants.Exceptions.MISSING_CONSENT));
    }
}
