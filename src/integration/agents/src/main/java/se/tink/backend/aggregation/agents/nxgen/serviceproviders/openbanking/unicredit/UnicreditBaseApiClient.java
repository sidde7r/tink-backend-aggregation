package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.unicredit.authenticator.entity.UnicreditConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.utils.BerlinGroupUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.Endpoints;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.PathParameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.configuration.UnicreditConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public abstract class UnicreditBaseApiClient {

    private static final DateTimeFormatter CONSENT_BODY_DATE_FORMATTER =
            DateTimeFormatter.ofPattern(Formats.DEFAULT_DATE_FORMAT);

    private static Logger logger = LoggerFactory.getLogger(UnicreditBaseApiClient.class);

    private final TinkHttpClient client;
    protected final PersistentStorage persistentStorage;
    private UnicreditConfiguration configuration;
    private final Credentials credentials;
    private final boolean requestIsManual;

    public UnicreditBaseApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            Credentials credentials,
            boolean requestIsManual) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
        this.requestIsManual = requestIsManual;
    }

    protected ConsentRequest getConsentRequest() {
        LocalDateTime validUntil =
                LocalDateTime.now().plusDays(FormValues.CONSENT_VALIDATION_PERIOD_IN_DAYS);

        return new ConsentRequest(
                new UnicreditConsentAccessEntity(FormValues.ALL_ACCOUNTS),
                true,
                CONSENT_BODY_DATE_FORMATTER.format(validUntil),
                FormValues.FREQUENCY_PER_DAY,
                false);
    }

    protected abstract Class<? extends ConsentResponse> getConsentResponseType();

    protected abstract URL getScaRedirectUrlFromConsentResponse(ConsentResponse consentResponse);

    protected UnicreditConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected Credentials getCredentials() {
        return Optional.ofNullable(credentials)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CREDENTIALS));
    }

    protected void setConfiguration(UnicreditConfiguration configuration) {
        this.configuration = configuration;
    }

    protected RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final String consentId = getConsentFromStorage();

        RequestBuilder requestBuilder =
                createRequest(url)
                        .header(HeaderKeys.X_REQUEST_ID, BerlinGroupUtils.getRequestId())
                        .header(HeaderKeys.CONSENT_ID, consentId);

        // This header must be present if the request was initiated by the PSU
        if (requestIsManual) {
            logger.info("Request is attended -- adding PSU header for {}", url);
            requestBuilder.header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS);
        } else {
            logger.info("Request is unattended -- omitting PSU header for {}", url);
        }

        return requestBuilder;
    }

    private String getConsentFromStorage() {
        return persistentStorage
                .get(StorageKeys.CONSENT_ID, String.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public URL buildAuthorizeUrl(String state) {

        ConsentResponse consentResponse =
                createRequest(new URL(getConfiguration().getBaseUrl() + Endpoints.CONSENTS))
                        .header(HeaderKeys.X_REQUEST_ID, BerlinGroupUtils.getRequestId())
                        .header(HeaderKeys.PSU_ID_TYPE, getConfiguration().getPsuIdType())
                        .header(
                                HeaderKeys.TPP_REDIRECT_URI,
                                new URL(getConfiguration().getRedirectUrl())
                                        .queryParam(HeaderKeys.STATE, state)
                                        .queryParam(HeaderKeys.CODE, HeaderValues.CODE))
                        .header(HeaderKeys.TPP_REDIRECT_PREFERED, false) // true for redirect auth
                        .post(getConsentResponseType(), getConsentRequest());

        persistentStorage.put(
                UnicreditConstants.StorageKeys.CONSENT_ID, consentResponse.getConsentId());

        return getScaRedirectUrlFromConsentResponse(consentResponse);
    }

    public ConsentStatusResponse getConsentStatus() throws SessionException {
        return createRequest(
                        new URL(getConfiguration().getBaseUrl() + Endpoints.CONSENT_STATUS)
                                .parameter(PathParameters.CONSENT_ID, getConsentIdFromStorage()))
                .header(HeaderKeys.X_REQUEST_ID, BerlinGroupUtils.getRequestId())
                .get(ConsentStatusResponse.class);
    }

    public String getConsentIdFromStorage() throws SessionException {
        return persistentStorage
                .get(StorageKeys.CONSENT_ID, String.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }

    public AccountsResponse fetchAccounts() {
        return createRequestInSession(new URL(getConfiguration().getBaseUrl() + Endpoints.ACCOUNTS))
                .get(AccountsResponse.class);
    }

    public BalancesResponse fetchAccountBalance(String accountId) {
        return createRequestInSession(
                        new URL(getConfiguration().getBaseUrl() + Endpoints.BALANCES)
                                .parameter(PathParameters.ACCOUNT_ID, accountId))
                .get(BalancesResponse.class);
    }

    public PaginatorResponse getTransactionsFor(TransactionalAccount account) {
        return createRequestInSession(
                        new URL(getConfiguration().getBaseUrl() + Endpoints.TRANSACTIONS)
                                .parameter(PathParameters.ACCOUNT_ID, account.getApiIdentifier()))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .queryParam(QueryKeys.DATE_FROM, getTransactionsDateFrom())
                .queryParam(
                        QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(new Date()))
                .get(TransactionsResponse.class);
    }

    protected abstract String getTransactionsDateFrom();

    public void removeConsentFromPersistentStorage() {
        persistentStorage.remove(StorageKeys.CONSENT_ID);
    }
}
