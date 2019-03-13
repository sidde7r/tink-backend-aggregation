package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.Format;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.UrlParameters;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.entity.consent.ConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import tink.org.apache.http.HttpHeaders;

public final class SwedbankApiClient {
    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private SwedbankConfiguration configuration;

    public SwedbankApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    public void setConfiguration(SwedbankConfiguration configuration) {
        this.configuration = configuration;
    }

    private SwedbankConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    private OAuth2Token getTokenFromSession() {
        return persistentStorage
                .get(SwedbankConstants.StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_TOKEN));
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        return createRequest(url)
                .header(
                        SwedbankConstants.HeaderKeys.CONSENT_ID,
                        persistentStorage.get(StorageKeys.CONSENT));
    }

    public FetchAccountResponse fetchAccounts() {

        ConsentResponse consentResponse = getConsent();
        persistentStorage.put(StorageKeys.CONSENT, consentResponse.getConsentId());

        return createRequestInSession(SwedbankConstants.Urls.ACCOUNTS)
                .addBearerToken(getTokenFromSession())
                .header(SwedbankConstants.HeaderKeys.DATE, getHeaderTimeStamp())
                .header(SwedbankConstants.HeaderKeys.X_REQUEST_ID, getRequestId())
                .queryParam(SwedbankConstants.QueryKeys.BIC, SwedbankConstants.BICSandbox.SWEDEN)
                .get(FetchAccountResponse.class);
    }

    public URL getAuthorizeUrl(String state) {

        return createRequest(SwedbankConstants.Urls.AUTHORIZE)
                .header(SwedbankConstants.HeaderKeys.DATE, getHeaderTimeStamp())
                .queryParam(SwedbankConstants.QueryKeys.BIC, SwedbankConstants.BICSandbox.SWEDEN)
                .queryParam(SwedbankConstants.QueryKeys.STATE, state)
                .queryParam(SwedbankConstants.QueryKeys.CLIENT_ID, getConfiguration().getClientId())
                .queryParam(
                        SwedbankConstants.QueryKeys.REDIRECT_URI,
                        getConfiguration().getRedirectUrl())
                .queryParam(
                        SwedbankConstants.QueryKeys.RESPONSE_TYPE,
                        SwedbankConstants.QueryValues.RESPONSE_TYPE_TOKEN)
                .queryParam(SwedbankConstants.QueryKeys.SCOPE, SwedbankConstants.QueryValues.SCOPE)
                .getUrl();
    }

    private ConsentResponse getConsent() {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 3);
        String consentValidityTimestamp =
                new SimpleDateFormat(Format.CONSENT_VALIDITY_TIMESTAMP).format(cal.getTime());

        ConsentRequest consentRequest =
                new ConsentRequest(
                        false,
                        consentValidityTimestamp,
                        100,
                        false,
                        new ConsentAccessEntity(null, null, null, "allAccounts", null));

        return client.request(SwedbankConstants.Urls.CONSENTS)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .addBearerToken(getTokenFromSession())
                .header(SwedbankConstants.HeaderKeys.TPP_TRANSACTION_ID, getRequestId())
                .header(SwedbankConstants.HeaderKeys.TPP_REQUEST_ID, getRequestId())
                .header(SwedbankConstants.HeaderKeys.X_REQUEST_ID, getRequestId())
                .header(SwedbankConstants.HeaderKeys.DATE, getHeaderTimeStamp())
                .queryParam(SwedbankConstants.QueryKeys.BIC, SwedbankConstants.BICSandbox.SWEDEN)
                .post(ConsentResponse.class, consentRequest);
    }

    public OAuth2Token getToken(String code) {

        TokenRequest request =
                new TokenRequest(
                        getConfiguration().getClientId(),
                        getConfiguration().getClientSecret(),
                        getConfiguration().getRedirectUrl(),
                        code,
                        SwedbankConstants.QueryValues.GRANT_TYPE,
                        SwedbankConstants.QueryValues.SCOPE);

        return client.request(SwedbankConstants.Urls.TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .header(SwedbankConstants.HeaderKeys.DATE, getHeaderTimeStamp())
                .queryParam(SwedbankConstants.QueryKeys.BIC, SwedbankConstants.BICSandbox.SWEDEN)
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponse.class, request.toData())
                .toTinkToken();
    }

    public AccountBalanceResponse getAccountBalance(String accountId) {

        return createRequestInSession(
                        Urls.ACCOUNT_BALANCES.parameter(UrlParameters.ACCOUNT_ID, accountId))
                .queryParam(SwedbankConstants.QueryKeys.BIC, SwedbankConstants.BICSandbox.SWEDEN)
                .header(SwedbankConstants.HeaderKeys.DATE, getHeaderTimeStamp())
                .addBearerToken(getTokenFromSession())
                .header(SwedbankConstants.HeaderKeys.X_REQUEST_ID, getRequestId())
                .get(AccountBalanceResponse.class);
    }

    public FetchTransactionsResponse getTransactions(String accountId, Date fromDate, Date toDate) {

        return createRequestInSession(
                        Urls.ACCOUNT_TRANSACTIONS.parameter(UrlParameters.ACCOUNT_ID, accountId))
                .queryParam(SwedbankConstants.QueryKeys.BIC, SwedbankConstants.BICSandbox.SWEDEN)
                .header(SwedbankConstants.HeaderKeys.DATE, getHeaderTimeStamp())
                .addBearerToken(getTokenFromSession())
                .header(SwedbankConstants.HeaderKeys.FROM_DATE, fromDate)
                .header(SwedbankConstants.HeaderKeys.TO_DATE, toDate)
                .header(SwedbankConstants.HeaderKeys.X_REQUEST_ID, getRequestId())
                .get(FetchTransactionsResponse.class);
    }

    public OAuth2Token refreshToken(String refreshToken) throws SessionException {
        RefreshRequest refreshRequest =
                new RefreshRequest(
                        refreshToken,
                        getConfiguration().getClientId(),
                        getConfiguration().getClientSecret(),
                        getConfiguration().getRedirectUrl());

        return client.request(SwedbankConstants.Urls.TOKEN)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, refreshRequest.toData())
                .toTinkToken();
    }

    private String getRequestId() {
        return java.util.UUID.randomUUID().toString();
    }

    private String getHeaderTimeStamp() {
        return new SimpleDateFormat(SwedbankConstants.Format.HEADER_TIMESTAMP).format(new Date());
    }
}
