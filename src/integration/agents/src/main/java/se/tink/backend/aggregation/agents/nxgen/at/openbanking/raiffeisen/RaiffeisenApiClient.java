package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.ParameterKeys;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.configuration.RaiffeisenConfiguration;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.entity.ConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.entity.ConsentPayloadEntity;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class RaiffeisenApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;
    private RaiffeisenConfiguration configuration;

    public RaiffeisenApiClient(
            TinkHttpClient client, PersistentStorage persistentStorage, Credentials credentials) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
    }

    private RaiffeisenConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(RaiffeisenConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {

        return createRequest(url)
                .header(
                        RaiffeisenConstants.HeaderKeys.AUTHORIZATION,
                        RaiffeisenConstants.HeaderValues.TOKEN_PREFIX + getTokenFromStorage());
    }

    private String getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, String.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public TokenResponse authenticate() {

        TokenRequest client_credentials =
                TokenRequest.builder()
                        .setGrantType(RaiffeisenConstants.FormValues.GRANT_TYPE)
                        .setScope(RaiffeisenConstants.FormValues.SCOPE)
                        .build();

        return client.request(Urls.AUTHENTICATE)
                .addBasicAuth(configuration.getClientId(), configuration.getClientSecret())
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, client_credentials);
    }

    public ConsentResponse getConsent() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Now use today date.
        c.add(Calendar.DATE, 1); // Adds 1 day

        SimpleDateFormat formatter =
                new SimpleDateFormat(RaiffeisenConstants.Formats.CONSENT_DATE_FORMAT);

        ConsentRequest consentRequest =
                new ConsentRequest(
                        new ConsentAccessEntity(
                                Collections.singletonList(
                                        new ConsentPayloadEntity(
                                                credentials.getField(
                                                        RaiffeisenConstants.CredentialKeys.IBAN))),
                                Collections.emptyList()),
                        false,
                        formatter.format(c.getTime()),
                        4);

        return createRequestInSession(Urls.CONSENTS)
                .header(HeaderKeys.X_REQUEST_ID, getRequestId())
                .post(ConsentResponse.class, consentRequest);
    }

    public AccountsResponse fetchAccounts() {

        return createRequestInSession(Urls.ACCOUNTS)
                .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID))
                .header(HeaderKeys.X_REQUEST_ID, RaiffeisenConstants.HeaderValues.X_REQUEST_ID)
                .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                .get(AccountsResponse.class);
    }

    private String getRequestId() {
        return java.util.UUID.randomUUID().toString();
    }

    public PaginatorResponse getTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {

        return createRequestInSession(
                        Urls.TRANSACTIONS.parameter(
                                ParameterKeys.ACCOUNT_ID, account.getApiIdentifier()))
                .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID))
                .header(HeaderKeys.X_REQUEST_ID, RaiffeisenConstants.HeaderValues.X_REQUEST_ID)
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .get(TransactionsResponse.class);
    }
}
