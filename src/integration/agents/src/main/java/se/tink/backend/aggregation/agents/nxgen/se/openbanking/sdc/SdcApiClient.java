package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc;

import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.SdcConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.SdcConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.SdcConstants.PathParameters;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.SdcConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.SdcConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.SdcConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.SdcConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.configuration.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.utils.BerlinGroupUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
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

    protected void setConfiguration(SdcConfiguration configuration) {
        this.configuration = configuration;
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
        return createRequest(SdcConstants.Urls.AUTHORIZATION)
                .queryParam(QueryKeys.SCOPE, SdcConstants.HeaderValues.SCOPE_AIS)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                .queryParam(QueryKeys.REDIRECT_URI, getConfiguration().getRedirectUrl())
                .queryParam(QueryKeys.CLIENT_ID, getConfiguration().getClientId())
                .queryParam(QueryKeys.STATE, state)
                .getUrl();
    }

    public OAuth2Token exchangeAuthorizationCode(String code) {

        TokenRequest tokenRequest =
                new TokenRequest(
                        SdcConstants.FormValues.AUTHORIZATION_CODE,
                        code,
                        getConfiguration().getRedirectUrl(),
                        getConfiguration().getClientId(),
                        getConfiguration().getClientSecret(),
                        SdcConstants.FormValues.SCOPE_AIS,
                        credentials.getField(Key.LOGIN_INPUT),
                        "");

        return createRequest(Urls.TOKEN)
                .header(HeaderKeys.X_REQUEST_ID, BerlinGroupUtils.getRequestId())
                .post(TokenResponse.class, tokenRequest)
                .toTinkToken();
    }

    public AccountsResponse fetchAccounts() {
        return createRequestInSession(Urls.ACCOUNTS)
                .header(
                        BerlinGroupConstants.HeaderKeys.X_REQUEST_ID,
                        BerlinGroupUtils.getRequestId())
                .header(BerlinGroupConstants.HeaderKeys.CONSENT_ID, BerlinGroupUtils.getRequestId())
                .header(
                        SdcConstants.HeaderKeys.OCP_APIM_SUBSCRIPTION_KEY,
                        getConfiguration().getOcpApimSubscriptionKey())
                .queryParam(BerlinGroupConstants.QueryKeys.WITH_BALANCE, String.valueOf(true))
                .get(AccountsResponse.class);
    }

    public BalancesResponse fetchAccountBalances(String accountId) {
        return createRequestInSession(Urls.BALANCES.parameter(PathParameters.ACCOUNT_ID, accountId))
                .header(
                        BerlinGroupConstants.HeaderKeys.X_REQUEST_ID,
                        BerlinGroupUtils.getRequestId())
                .header(BerlinGroupConstants.HeaderKeys.CONSENT_ID, BerlinGroupUtils.getRequestId())
                .header(
                        SdcConstants.HeaderKeys.OCP_APIM_SUBSCRIPTION_KEY,
                        getConfiguration().getOcpApimSubscriptionKey())
                .get(BalancesResponse.class);
    }

    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return createRequestInSession(
                        Urls.TRANSACTIONS.parameter(
                                PathParameters.ACCOUNT_ID, account.getApiIdentifier()))
                .header(
                        BerlinGroupConstants.HeaderKeys.X_REQUEST_ID,
                        BerlinGroupUtils.getRequestId())
                .header(BerlinGroupConstants.HeaderKeys.CONSENT_ID, BerlinGroupUtils.getRequestId())
                .header(
                        SdcConstants.HeaderKeys.OCP_APIM_SUBSCRIPTION_KEY,
                        getConfiguration().getOcpApimSubscriptionKey())
                .queryParam(
                        BerlinGroupConstants.QueryKeys.BOOKING_STATUS,
                        SdcConstants.QueryValues.BOOKED) // TODO Verify pagination
                .queryParam(
                        SdcConstants.QueryKeys.DATE_FROM,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .get(TransactionsResponse.class);
    }
}
