package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.CredentialsKeys;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.entity.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.entity.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.configuration.DnbConfiguration;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class DnbApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final Credentials credentials;
    private DnbConfiguration configuration;

    public DnbApiClient(
            final TinkHttpClient client,
            final SessionStorage sessionStorage,
            final Credentials credentials) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
    }

    private DnbConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(DnbConfiguration configuration) {
        this.configuration = configuration;
    }

    public AccountsResponse fetchAccounts() {
        return createRequest(new URL(getConfiguration().getBaseUrl() + Urls.ACCOUNTS))
                .header(DnbConstants.HeaderKeys.CONSENT_ID, getConsentId())
                .get(AccountsResponse.class);
    }

    public TransactionResponse fetchTransactions(final String accountId) {
        return createRequest(
                        new URL(
                                getConfiguration().getBaseUrl()
                                        + String.format(Urls.TRANSACTIONS, accountId)))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .header(HeaderKeys.CONSENT_ID, getConsentId())
                .get(TransactionResponse.class);
    }

    public URL getAuthorizeUrl(final String state) {
        sessionStorage.put(StorageKeys.STATE, state);
        final ConsentResponse consentResponse = getConsent();
        return new URL(consentResponse.getScaRedirectLink()).queryParam(QueryKeys.STATE, state);
    }

    public BalancesResponse fetchBalance(final String accountId) {
        return createRequest(
                        new URL(
                                getConfiguration().getBaseUrl()
                                        + String.format(Urls.BALANCES, accountId)))
                .header(HeaderKeys.CONSENT_ID, getConsentId())
                .get(BalancesResponse.class);
    }

    private ConsentResponse fetchConsent() {
        final ConsentRequest consentsRequest =
                ConsentRequest.builder()
                        .frequencyPerDay(5)
                        .recurringIndicator(true)
                        .combinedServiceIndicator(false)
                        .validUntil(getValidUntilForConsent())
                        .build();
        final URL url = new URL(getConfiguration().getBaseUrl() + Urls.CONSENTS);

        return createRequestWithRedirectStateAndCode(url)
                .body(consentsRequest.toData())
                .post(ConsentResponse.class);
    }

    private RequestBuilder createRequestWithoutRedirectHeader(final URL url) {
        final String requestId = UUID.randomUUID().toString();

        return client.request(url)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, requestId)
                .header(
                        DnbConstants.HeaderKeys.PSU_ID,
                        credentials.getField(CredentialsKeys.PSU_ID));
    }

    private RequestBuilder createRequest(final URL url) {
        return createRequestWithoutRedirectHeader(url)
                .header(HeaderKeys.TPP_REDIRECT_URI, getConfiguration().getRedirectUrl());
    }

    private RequestBuilder createRequestWithRedirectStateAndCode(final URL url) {
        final URL redirectUrl =
                new URL(getConfiguration().getRedirectUrl())
                        .queryParam(QueryKeys.STATE, sessionStorage.get(StorageKeys.STATE))
                        .queryParam(QueryKeys.CODE, "123456");

        return createRequestWithoutRedirectHeader(url)
                .header(HeaderKeys.TPP_REDIRECT_URI, decodeUrl(redirectUrl));
    }

    private ConsentResponse getConsent() {
        return sessionStorage
                .get(StorageKeys.CONSENT_OBJECT, ConsentResponse.class)
                .orElseGet(
                        () -> {
                            final ConsentResponse consentResponse = fetchConsent();
                            sessionStorage.put(StorageKeys.CONSENT_OBJECT, consentResponse);
                            return consentResponse;
                        });
    }

    private String getConsentId() {
        return getConsent().getConsentId();
    }

    private String decodeUrl(final URL url) {
        try {
            return URLDecoder.decode(url.toString(), UTF_8);
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalArgumentException(ErrorMessages.URL_ENCODING_ERROR);
        }
    }

    private Date getValidUntilForConsent() {
        final Calendar now = Calendar.getInstance();
        now.add(Calendar.MONTH, 11);
        return now.getTime();
    }
}
