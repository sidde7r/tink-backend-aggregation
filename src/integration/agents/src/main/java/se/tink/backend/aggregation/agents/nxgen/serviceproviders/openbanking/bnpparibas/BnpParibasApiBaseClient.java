package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.configuration.BnpParibasConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.utils.BnpParibasSignatureHeaderProvider;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BnpParibasApiBaseClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final BnpParibasSignatureHeaderProvider bnpParibasSignatureHeaderProvider;
    private final BnpParibasConfiguration bnpParibasConfiguration;

    public BnpParibasApiBaseClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            BnpParibasConfiguration bnpParibasConfiguration,
            BnpParibasSignatureHeaderProvider bnpParibasSignatureHeaderProvider) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.bnpParibasConfiguration = bnpParibasConfiguration;
        this.bnpParibasSignatureHeaderProvider = bnpParibasSignatureHeaderProvider;
    }

    public URL getAuthorizeUrl(String state) {
        return client.request(new URL(bnpParibasConfiguration.getAuthorizeUrl()))
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
    }

    private String getAuthorizationString() {
        return String.format(
                "%s:%s",
                bnpParibasConfiguration.getClientId(), bnpParibasConfiguration.getClientSecret());
    }

    private RequestBuilder createRequestInSession(URL url) {
        String reqId = UUID.randomUUID().toString();
        String signature =
                bnpParibasSignatureHeaderProvider.buildSignatureHeader(
                        sessionStorage.get(StorageKeys.TOKEN), reqId, bnpParibasConfiguration);

        return client.request(url)
                .addBearerToken(
                        getTokenFromSession()
                                .orElseThrow(
                                        () ->
                                                new IllegalArgumentException(
                                                        BnpParibasBaseConstants.ErrorMessages
                                                                .MISSING_TOKEN)))
                .header(BnpParibasBaseConstants.HeaderKeys.SIGNATURE, signature)
                .header(BnpParibasBaseConstants.HeaderKeys.X_REQUEST_ID, reqId)
                .accept(MediaType.APPLICATION_JSON)
                .header(
                        BnpParibasBaseConstants.RegisterUtils.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON);
    }

    private Optional<OAuth2Token> getTokenFromSession() {
        return sessionStorage.get(BnpParibasBaseConstants.StorageKeys.TOKEN, OAuth2Token.class);
    }

    public TokenResponse exchangeAuthorizationToken(AbstractForm request) {
        return client.request(new URL(bnpParibasConfiguration.getTokenUrl()))
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
        return client.request(new URL(bnpParibasConfiguration.getTokenUrl()))
                .header(
                        BnpParibasBaseConstants.HeaderKeys.AUTHORIZATION,
                        BnpParibasBaseConstants.HeaderValues.BASIC
                                + Base64.getEncoder()
                                        .encodeToString(getAuthorizationString().getBytes()))
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, request.toData())
                .toOauthToken();
    }

    public AccountsResponse fetchAccounts() {
        HttpResponse httpResponse =
                createRequestInSession(
                                new URL(bnpParibasConfiguration.getBaseUrl() + Urls.ACCOUNTS_PATH))
                        .get(HttpResponse.class);

        return extractBody(httpResponse, AccountsResponse.class).orElse(new AccountsResponse());
    }

    public BalanceResponse getBalance(String resourceId) {
        HttpResponse httpResponse =
                createRequestInSession(
                                new URL(bnpParibasConfiguration.getBaseUrl() + Urls.BALANCES_PATH)
                                        .parameter(IdTags.ACCOUNT_RESOURCE_ID, resourceId))
                        .get(HttpResponse.class);

        return extractBody(httpResponse, BalanceResponse.class).orElse(new BalanceResponse());
    }

    public TransactionsResponse getTransactions(String resourceId, Date dateFrom, Date dateTo) {
        SimpleDateFormat sdf = new SimpleDateFormat(DateFormatUtils.ISO_DATE_FORMAT.getPattern());

        HttpResponse httpResponse =
                createRequestInSession(
                                new URL(
                                                bnpParibasConfiguration.getBaseUrl()
                                                        + Urls.TRANSACTIONS_PATH)
                                        .parameter(IdTags.ACCOUNT_RESOURCE_ID, resourceId))
                        .queryParam(QueryKeys.DATE_FROM, sdf.format(dateFrom))
                        .queryParam(QueryKeys.DATE_TO, sdf.format(dateTo))
                        .get(HttpResponse.class);

        return extractBody(httpResponse, TransactionsResponse.class)
                .orElse(new TransactionsResponse());
    }

    public EndUserIdentityResponse getEndUserIdentity() {
        return createRequestInSession(
                        new URL(
                                bnpParibasConfiguration.getBaseUrl()
                                        + BnpParibasBaseConstants.Urls.FETCH_USER_IDENTITY_DATA))
                .get(EndUserIdentityResponse.class);
    }

    private <T> Optional<T> extractBody(HttpResponse response, Class<T> clazz) {
        if (response.getStatus() == HttpStatus.SC_NO_CONTENT) {
            return Optional.empty();
        } else {
            return Optional.of(response.getBody(clazz));
        }
    }
}
