package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc;

import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.PathParameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.authenticator.rpc.RefreshAccessTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.configuration.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class SdcApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;
    private SdcConfiguration configuration;

    public SdcApiClient(
            TinkHttpClient client, PersistentStorage persistentStorage, Credentials credentials) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
    }

    private SdcConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(
            SdcConfiguration configuration, EidasProxyConfiguration eidasProxyConfiguration) {
        this.configuration = configuration;
        this.client.setEidasProxy(eidasProxyConfiguration);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromStorage();

        return createRequest(url).addBearerToken(authToken);
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public URL buildAuthorizeUrl(String state) {
        return createRequest(Urls.AUTHORIZATION)
                .queryParam(QueryKeys.SCOPE, HeaderValues.SCOPE_AIS)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                .queryParam(QueryKeys.REDIRECT_URI, getConfiguration().getRedirectUrl())
                .queryParam(QueryKeys.CLIENT_ID, getConfiguration().getClientId())
                .queryParam(QueryKeys.STATE, state)
                .getUrl();
    }

    public OAuth2Token exchangeAuthorizationCode(String code) {

        TokenRequest tokenRequest =
                new TokenRequest(
                        FormValues.AUTHORIZATION_CODE,
                        code,
                        getConfiguration().getRedirectUrl(),
                        getConfiguration().getClientId(),
                        getConfiguration().getClientSecret(),
                        FormValues.SCOPE_AIS,
                        credentials.getField(Key.LOGIN_INPUT),
                        "");

        return createRequest(Urls.TOKEN)
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .post(TokenResponse.class, tokenRequest)
                .toTinkToken();
    }

    public OAuth2Token refreshAccessToken(String refreshToken) {

        RefreshAccessTokenRequest tokenRequest =
                new RefreshAccessTokenRequest(
                        FormValues.REFRESH_TOKEN,
                        refreshToken,
                        getConfiguration().getRedirectUrl(),
                        getConfiguration().getClientId(),
                        getConfiguration().getClientSecret(),
                        FormValues.SCOPE_AIS);

        return createRequest(Urls.TOKEN)
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .post(TokenResponse.class, tokenRequest)
                .toTinkToken();
    }

    public AccountsResponse fetchAccounts() {
        return createRequestInSession(Urls.ACCOUNTS)
                .header(Psd2Headers.Keys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(Psd2Headers.Keys.CONSENT_ID, Psd2Headers.getRequestId())
                .header(
                        HeaderKeys.OCP_APIM_SUBSCRIPTION_KEY,
                        getConfiguration().getOcpApimSubscriptionKey())
                .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                .get(AccountsResponse.class);
    }

    public BalancesResponse fetchAccountBalances(String accountId) {
        return createRequestInSession(Urls.BALANCES.parameter(PathParameters.ACCOUNT_ID, accountId))
                .header(Psd2Headers.Keys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(Psd2Headers.Keys.CONSENT_ID, Psd2Headers.getRequestId())
                .header(
                        HeaderKeys.OCP_APIM_SUBSCRIPTION_KEY,
                        getConfiguration().getOcpApimSubscriptionKey())
                .get(BalancesResponse.class);
    }

    public TransactionsResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        /*
           Currently, for a given date range, if there are more than 200 transactions, the bank
           just returns the first 200 and ignores the rest and does not give any indicator to us
           to make us realise the issue. Bank will make a fix for that in 2020.
           For this reason we temporarily narrowed down the date range (from 3 months to 1 month
           see SdcAgent class)

           It is not a perfect solution as one might still have more than 200 transactions for a
           given date period. For this reason there are two things that we can do if we ever get
           200 transactions as a response of an API call:

           1) Crash the agent so we will be sure that we will not show any corrupted data (data with
           missing transactions) to the user

           2) Divide the date range into two, recursively call getTransactionsFor for these two
           smaller date ranges, merge the results and return

           3) Find the oldest date (D) in the fetched transactions and recursively fetch the transactions
           whose date period is between fromDate and D. Append it into the response

           We need to decide one of these approaches and implement it here
        */

        TransactionsResponse response =
                createRequestInSession(
                                Urls.TRANSACTIONS.parameter(
                                        PathParameters.ACCOUNT_ID, account.getApiIdentifier()))
                        .header(Psd2Headers.Keys.X_REQUEST_ID, Psd2Headers.getRequestId())
                        .header(Psd2Headers.Keys.CONSENT_ID, Psd2Headers.getRequestId())
                        .header(Psd2Headers.Keys.X_REQUEST_ID, Psd2Headers.getRequestId())
                        .header(Psd2Headers.Keys.CONSENT_ID, Psd2Headers.getRequestId())
                        .header(
                                HeaderKeys.OCP_APIM_SUBSCRIPTION_KEY,
                                getConfiguration().getOcpApimSubscriptionKey())
                        .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKED)
                        .queryParam(
                                QueryKeys.DATE_FROM,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                        .queryParam(
                                QueryKeys.DATE_TO,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                        .get(TransactionsResponse.class);

        // Implementation of approach (3) (this is a temporary fix, should be removed after the bank
        // fixes the issue in their end)
        Optional<Date> transactionsMissingUntil = response.getOverflowTransactionDate();

        if (transactionsMissingUntil.isPresent()) {
            TransactionsResponse responseForMissingPart =
                    getTransactionsFor(account, fromDate, transactionsMissingUntil.get());
            response.mergeTransactionResponse(responseForMissingPart);
        }

        return response;
    }
}
