package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.MessageCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.utls.CbiGlobeUtils;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CbiGlobeApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private CbiGlobeConfiguration configuration;
    private boolean requestManual;
    private static final Logger logger = LoggerFactory.getLogger(CbiGlobeApiClient.class);

    public CbiGlobeApiClient(
            TinkHttpClient client, PersistentStorage persistentStorage, boolean requestManual) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.requestManual = requestManual;
    }

    protected CbiGlobeConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(CbiGlobeConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url).type(MediaType.APPLICATION_JSON);
    }

    protected RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromStorage();

        return createRequest(url)
                .addBearerToken(authToken)
                .header(HeaderKeys.ASPSP_CODE, configuration.getAspspCode())
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .header(HeaderKeys.DATE, CbiGlobeUtils.getCurrentDateFormatted());
    }

    protected RequestBuilder createRequestWithConsent(URL url) {
        RequestBuilder rb =
                createRequestInSession(url)
                        .header(
                                HeaderKeys.CONSENT_ID,
                                persistentStorage.get(StorageKeys.CONSENT_ID));
        // only for testing, this commit will be reverted after tests
        logger.info("REQUEST MANUAL IS SET TO " + requestManual);
        if (requestManual) {
            rb.header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.DEFAULT_PSU_IP_ADDRESS);
        }

        return rb;
    }

    private RequestBuilder createRequestWithConsentSandbox(URL url) {
        return createRequestInSession(url)
                .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID));
    }

    protected RequestBuilder createAccountsRequestWithConsent() {
        return createRequestInSession(Urls.ACCOUNTS)
                .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID));
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        MessageCodes.NO_ACCESS_TOKEN_IN_STORAGE.name()));
    }

    public GetTokenResponse getToken(String authorizationHeader) {
        return createRequest(Urls.TOKEN)
                .header(HeaderKeys.AUTHORIZATION, authorizationHeader)
                .queryParam(QueryKeys.GRANT_TYPE, QueryValues.CLIENT_CREDENTIALS)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(GetTokenResponse.class);
    }

    public ConsentResponse createConsent(ConsentRequest consentRequest, String redirectUrl) {
        RequestBuilder request =
                createRequestInSession(Urls.CONSENTS)
                        .header(HeaderKeys.ASPSP_PRODUCT_CODE, configuration.getAspspProductCode())
                        .header(HeaderKeys.TPP_REDIRECT_URI, redirectUrl)
                        .header(HeaderKeys.TPP_NOK_REDIRECT_URI, configuration.getRedirectUrl());

        return request.post(ConsentResponse.class, consentRequest);
    }

    public GetAccountsResponse getAccounts() {
        return createAccountsRequestWithConsent().get(GetAccountsResponse.class);
    }

    public GetBalancesResponse getBalances(String resourceId) {
        try {
            return createRequestWithConsent(Urls.BALANCES.parameter(IdTags.ACCOUNT_ID, resourceId))
                    .get(GetBalancesResponse.class);
        } catch (HttpResponseException e) {
            handleAccessExceededError(e);
            throw e;
        }
    }

    public GetTransactionsResponse getTransactions(
            String apiIdentifier, Date fromDate, Date toDate, String bookingType) {
        try {
            return createRequestWithConsent(
                            Urls.TRANSACTIONS.parameter(IdTags.ACCOUNT_ID, apiIdentifier))
                    .queryParam(QueryKeys.BOOKING_STATUS, bookingType)
                    .queryParam(
                            QueryKeys.DATE_FROM,
                            ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                    .queryParam(
                            QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                    .get(GetTransactionsResponse.class);
        } catch (HttpResponseException e) {
            handleAccessExceededError(e);
            throw e;
        }
    }

    public void handleAccessExceededError(HttpResponseException e) {
        final String message = e.getResponse().getBody(String.class).toLowerCase();
        if (message.contains(ErrorMessages.ACCESS_EXCEEDED)) {
            throw BankServiceError.ACCESS_EXCEEDED.exception();
        }
    }

    public boolean isTokenValid() {
        return getTokenFromStorage().isValid();
    }

    public ConsentResponse getConsentStatus(String consentType) throws SessionException {
        return createRequestInSession(
                        Urls.CONSENTS_STATUS.parameter(
                                IdTags.CONSENT_ID, getConsentIdFromStorage(consentType)))
                .get(ConsentResponse.class);
    }

    public String getConsentIdFromStorage(String consentType) throws SessionException {
        return persistentStorage
                .get(consentType, String.class)
                .orElseThrow(() -> SessionError.SESSION_EXPIRED.exception());
    }

    public void removeAccountsFromStorage() {
        persistentStorage.remove(StorageKeys.ACCOUNTS);
    }

    public void removeConsentFromPersistentStorage() {
        persistentStorage.remove(StorageKeys.CONSENT_ID);
    }

    public CreatePaymentResponse createPayment(CreatePaymentRequest createPaymentRequest) {
        URL redirectUrl =
                new URL(configuration.getRedirectUrl())
                        .queryParam(QueryKeys.STATE, QueryValues.STATE);
        return createRequestWithConsentSandbox(Urls.PAYMENT)
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.DEFAULT_PSU_IP_ADDRESS)
                .header(HeaderKeys.ASPSP_PRODUCT_CODE, configuration.getAspspProductCode())
                .header(HeaderKeys.TPP_REDIRECT_URI, redirectUrl)
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    public CreatePaymentResponse getPayment(String uniqueId) {
        return createRequestWithConsentSandbox(
                        Urls.FETCH_PAYMENT.parameter(IdTags.PAYMENT_ID, uniqueId))
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.DEFAULT_PSU_IP_ADDRESS)
                .header(HeaderKeys.ASPSP_PRODUCT_CODE, configuration.getAspspProductCode())
                .get(CreatePaymentResponse.class);
    }

    public GetAccountsResponse fetchAccounts() {
        GetAccountsResponse getAccountsResponse =
                SerializationUtils.deserializeFromString(
                        persistentStorage.get(StorageKeys.ACCOUNTS), GetAccountsResponse.class);

        if (getAccountsResponse == null) {
            getAccountsResponse = getAccounts();
            persistentStorage.put(StorageKeys.ACCOUNTS, getAccountsResponse);
        }
        return getAccountsResponse;
    }
}
