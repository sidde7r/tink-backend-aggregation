package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen;

import java.time.LocalDate;
import java.util.Date;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.ErrorTexts;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.ParameterKeys;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.entity.ConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.configuration.RaiffeisenConfiguration;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class RaiffeisenApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;
    private AgentConfiguration<RaiffeisenConfiguration> configuration;
    private final SessionStorage sessionStorage;

    public RaiffeisenApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            Credentials credentials,
            SessionStorage sessionStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
        this.sessionStorage = sessionStorage;
    }

    protected void setConfiguration(AgentConfiguration<RaiffeisenConfiguration> configuration) {
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
                        RaiffeisenConstants.HeaderValues.TOKEN_PREFIX.concat(
                                getTokenFromStorage().getAccessToken()))
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId());
    }

    private OAuth2Token getTokenFromStorage() {
        return sessionStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_TOKEN));
    }

    public OAuth2Token getToken() {
        TokenRequest clientCredentials =
                TokenRequest.builder()
                        .setGrantType(RaiffeisenConstants.FormValues.GRANT_TYPE)
                        .setScope(RaiffeisenConstants.FormValues.SCOPE)
                        .setClientId(configuration.getProviderSpecificConfiguration().getClientId())
                        .build();

        return client.request(Urls.AUTHENTICATE)
                .header(HeaderKeys.CACHE_CONTROL, HeaderValues.CACHE_CONTROL)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, clientCredentials)
                .toTinkToken();
    }

    public URL getAuthorizeUrl(String state) {

        // IBAN flow.
        ConsentRequest consentRequest =
                new ConsentRequest(
                        new ConsentAccessEntity(credentials.getField(CredentialKeys.IBAN)),
                        true,
                        LocalDate.now()
                                .plusDays(RaiffeisenConstants.BodyValues.CONSENT_DAYS_VALID)
                                .toString(),
                        RaiffeisenConstants.BodyValues.FREQUENCY_PER_DAY,
                        false);

        try {
            ConsentResponse consentResponse =
                    createRequestInSession(Urls.CONSENTS)
                            .header(HeaderKeys.CACHE_CONTROL, HeaderValues.CACHE_CONTROL)
                            .header(HeaderKeys.TPP_REDIRECT_URI, createRedirectUrlWithState(state))
                            .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                            .post(ConsentResponse.class, consentRequest);

            persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());
            return new URL(consentResponse.getRedirectUrl());

        } catch (HttpResponseException e) {
            String errorMessage = e.getResponse().getBody(ErrorResponse.class).getErrorText();
            if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED
                    && errorMessage
                            .toLowerCase()
                            .contains(RaiffeisenConstants.ErrorTexts.INVALID_IBAN)) {
                throw new IllegalArgumentException(ErrorMessages.INVALID_IBAN);
            }
            throw e;
        }
    }

    private String createRedirectUrlWithState(String state) {
        return new URL(configuration.getRedirectUrl())
                .queryParam(QueryKeys.STATE, state)
                .toString();
    }

    public AccountsResponse fetchAccounts() {
        try {
            return createRequestInSession(Urls.ACCOUNTS)
                    .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID))
                    .header(HeaderKeys.CACHE_CONTROL, HeaderValues.CACHE_CONTROL)
                    .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                    .get(AccountsResponse.class);
        } catch (HttpResponseException e) {
            ErrorResponse errorMessage = e.getResponse().getBody(ErrorResponse.class);
            if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED
                    && errorMessage.getErrorCode().equalsIgnoreCase(ErrorMessages.CONSENT_UNKNOWN)
                    && errorMessage
                            .getErrorText()
                            .toUpperCase()
                            .contains(ErrorTexts.ACCESS_EXCEEDED_TEXT)) {
                throw BankServiceError.ACCESS_EXCEEDED.exception();
            }
            throw e;
        }
    }

    public PaginatorResponse fetchTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {

        return createRequestInSession(
                        Urls.TRANSACTIONS.parameter(
                                ParameterKeys.ACCOUNT_ID, account.getApiIdentifier()))
                .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID))
                .header(HeaderKeys.CACHE_CONTROL, HeaderValues.CACHE_CONTROL)
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .get(TransactionsResponse.class);
    }

    public CreatePaymentResponse createPayment(CreatePaymentRequest createPaymentRequest) {

        return createRequestInSession(Urls.INITIATE_PAYMENT)
                .header(HeaderKeys.X_REQUEST_ID, HeaderValues.X_REQUEST_ID)
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    public GetPaymentResponse getPayment(String paymentId) throws HttpResponseException {

        GetPaymentResponse getPaymentResponse =
                createRequestInSession(Urls.GET_PAYMENT.parameter(IdTags.PAYMENT_ID, paymentId))
                        .header(HeaderKeys.X_REQUEST_ID, HeaderValues.X_REQUEST_ID)
                        .get(GetPaymentResponse.class);
        getPaymentResponse.setUniqueID(paymentId);
        return getPaymentResponse;
    }

    public String getConsentStatus() {
        return createRequestInSession(
                        Urls.CONSENT_STATUS.parameter(
                                ParameterKeys.CONSENT_ID,
                                persistentStorage.get(StorageKeys.CONSENT_ID)))
                .get(ConsentResponse.class)
                .getConsentStatus();
    }
}
