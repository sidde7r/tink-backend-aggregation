package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.configuration.BnpParibasConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;

public class BnpParibasApiBaseClient {

    private final SessionStorage sessionStorage;
    private final TinkHttpClient client;
    private BnpParibasConfiguration bnpParibasConfiguration;
    private final String bank;

    public BnpParibasApiBaseClient(
            TinkHttpClient client, SessionStorage sessionStorage, String bank) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.bank = bank;
    }

    public URL getAuthorizeUrl(String state) {

        URL url =
                client.request(
                                new URL(BnpParibasBaseConstants.Urls.AUTHENTICATION_URL)
                                        .parameter(BnpParibasBaseConstants.IdTags.BANK, bank))
                        .queryParam(
                                BnpParibasBaseConstants.QueryKeys.CLIENT_ID,
                                bnpParibasConfiguration.getClientId())
                        .queryParam(
                                BnpParibasBaseConstants.QueryKeys.RESPONSE_TYPE,
                                BnpParibasBaseConstants.QueryValues.CODE)
                        .queryParam(
                                BnpParibasBaseConstants.QueryKeys.SCOPE,
                                BnpParibasBaseConstants.QueryValues.FULL_SCOPES)
                        .queryParam(
                                BnpParibasBaseConstants.QueryKeys.REDIRECT_URI,
                                bnpParibasConfiguration.getRedirectUrl())
                        .queryParam(BnpParibasBaseConstants.QueryKeys.STATE, state)
                        .getUrl();
        return url;
    }

    public String getAuthorizationString() {
        return bnpParibasConfiguration.getClientId()
                + ":"
                + bnpParibasConfiguration.getClientSecret();
    }

    private RequestBuilder createRequestInSession(URL url, String signature, String reqId) {
        return client.request(url.parameter(BnpParibasBaseConstants.IdTags.BANK, bank))
                .addBearerToken(getTokenFromSession())
                .header(BnpParibasBaseConstants.HeaderKeys.SIGNATURE, signature)
                .header(BnpParibasBaseConstants.HeaderKeys.X_REQUEST_ID, reqId)
                .accept(MediaType.APPLICATION_JSON);
    }

    public TokenResponse exchangeAuthorizationToken(TokenRequest request) {
        return client.request(
                        new URL(BnpParibasBaseConstants.Urls.TOKEN_URL)
                                .parameter(BnpParibasBaseConstants.IdTags.BANK, bank))
                .body(request, MediaType.APPLICATION_FORM_URLENCODED)
                .header(
                        BnpParibasBaseConstants.HeaderKeys.AUTHORIZATION,
                        BnpParibasBaseConstants.HeaderValues.BASIC
                                + Base64.getEncoder()
                                        .encodeToString(getAuthorizationString().getBytes()))
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponse.class);
    }

    public OAuth2Token exchangeRefreshToken(RefreshRequest request) {
        return client.request(
                        new URL(BnpParibasBaseConstants.Urls.TOKEN_URL)
                                .parameter(BnpParibasBaseConstants.IdTags.BANK, bank))
                .header(
                        BnpParibasBaseConstants.HeaderKeys.AUTHORIZATION,
                        BnpParibasBaseConstants.HeaderValues.BASIC
                                + Base64.getEncoder()
                                        .encodeToString(getAuthorizationString().getBytes()))
                .body(request, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class)
                .toOauthToken();
    }

    public AccountsResponse fetchAccounts(String signature, String reqId) {
        return createRequestInSession(
                        new URL(BnpParibasBaseConstants.Urls.ACCOUNTS_PATH), signature, reqId)
                .get(AccountsResponse.class);
    }

    public void setConfiguration(BnpParibasConfiguration bnpParibasConfiguration) {
        this.bnpParibasConfiguration = bnpParibasConfiguration;
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(BnpParibasBaseConstants.StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        BnpParibasBaseConstants.ErrorMessages.MISSING_TOKEN));
    }

    public BalanceResponse getBalance(String resourceId, String signature, String reqId) {
        return createRequestInSession(
                        new URL(BnpParibasBaseConstants.Urls.BALANCES_PATH)
                                .parameter(
                                        BnpParibasBaseConstants.IdTags.ACCOUNT_RESOURCE_ID,
                                        resourceId),
                        signature,
                        reqId)
                .get(BalanceResponse.class);
    }

    public TransactionsResponse getTransactions(
            String resourceId, String signature, String reqId, Date dateFrom, Date dateTo) {

        SimpleDateFormat sdf =
                new SimpleDateFormat(
                        BnpParibasBaseConstants.QueryValues.FORMATTER_MILLI_WITH_TIMEZONE);
        sdf.setTimeZone(TimeZone.getTimeZone(BnpParibasBaseConstants.QueryValues.TIMEZONE));

        return createRequestInSession(
                        new URL(BnpParibasBaseConstants.Urls.TRANSACTIONS_PATH)
                                .parameter(
                                        BnpParibasBaseConstants.IdTags.ACCOUNT_RESOURCE_ID,
                                        resourceId),
                        signature,
                        reqId)
                .queryParamRaw(BnpParibasBaseConstants.QueryKeys.DATE_FROM, sdf.format(dateFrom))
                .queryParamRaw(BnpParibasBaseConstants.QueryKeys.DATE_TO, sdf.format(dateTo))
                .get(TransactionsResponse.class);
    }
}
