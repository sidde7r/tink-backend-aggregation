package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.CredentialsKeys;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.entity.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.entity.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.entity.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.enums.DnbPaymentType;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc.CreditCardAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc.CreditCardTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class DnbApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;
    private final String redirectUrl;

    public DnbApiClient(
            final TinkHttpClient client,
            final SessionStorage sessionStorage,
            final PersistentStorage persistentStorage,
            final Credentials credentials,
            String redirectUrl) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
        this.redirectUrl = redirectUrl;
    }

    public URL getAuthorizeUrl(final String state) {
        sessionStorage.put(StorageKeys.STATE, state);
        ConsentResponse consentResponse = createConsent();
        persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());
        return new URL(consentResponse.getScaRedirectLink()).queryParam(QueryKeys.STATE, state);
    }

    public HttpResponse fetchAccounts() {
        return createRequest(new URL(DnbConstants.BASE_URL.concat(Urls.ACCOUNTS)))
                .header(DnbConstants.HeaderKeys.CONSENT_ID, getConsentId())
                .get(HttpResponse.class);
    }

    public HttpResponse fetchBalances(final String accountId) {
        return createRequest(
                        new URL(
                                DnbConstants.BASE_URL.concat(
                                        String.format(Urls.BALANCES, accountId))))
                .header(HeaderKeys.CONSENT_ID, getConsentId())
                .get(HttpResponse.class);
    }

    public PaginatorResponse fetchCreditCardTransactions(
            CreditCardAccount account, Date fromDate, Date toDate) {
        try {
            return createRequest(
                            new URL(
                                    DnbConstants.BASE_URL.concat(
                                            String.format(
                                                    Urls.CREDIT_CARD_TRANSACTION,
                                                    account.getApiIdentifier()))))
                    .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                    .queryParam(
                            QueryKeys.FROM_DATE,
                            ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                    .queryParam(
                            QueryKeys.TO_DATE, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                    .header(HeaderKeys.CONSENT_ID, getConsentId())
                    .get(CreditCardTransactionResponse.class);
        } catch (HttpResponseException e) {
            return PaginatorResponseImpl.createEmpty(false);
        }
    }

    public CreditCardAccountResponse fetchCreditCardAccounts() {
        return createRequest(new URL(DnbConstants.BASE_URL.concat(Urls.CREDIT_CARDS)))
                .header(DnbConstants.HeaderKeys.CONSENT_ID, getConsentId())
                .get(CreditCardAccountResponse.class);
    }

    public PaginatorResponse fetchTransactions(final String accountId, Date fromDate, Date toDate) {
        try {
            return createRequest(
                            new URL(
                                    DnbConstants.BASE_URL.concat(
                                            String.format(Urls.TRANSACTIONS, accountId))))
                    .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                    .queryParam(
                            QueryKeys.FROM_DATE,
                            ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                    .queryParam(
                            QueryKeys.TO_DATE, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                    .header(HeaderKeys.CONSENT_ID, getConsentId())
                    .get(TransactionResponse.class);
        } catch (HttpResponseException e) {
            return PaginatorResponseImpl.createEmpty(false);
        }
    }

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, DnbPaymentType dnbPaymentType) {
        return createRequest(
                        new URL(DnbConstants.BASE_URL.concat(Urls.PAYMENTS))
                                .parameter(IdTags.PAYMENT_TYPE, dnbPaymentType.toString()))
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    public GetPaymentResponse getPayment(DnbPaymentType dnbPaymentType, String paymentId) {
        return createRequestWithoutRedirectHeader(
                        new URL(DnbConstants.BASE_URL.concat(Urls.GET_PAYMENT))
                                .parameter(IdTags.PAYMENT_TYPE, dnbPaymentType.toString())
                                .parameter(IdTags.PAYMENT_ID, paymentId))
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                .get(GetPaymentResponse.class);
    }

    private RequestBuilder createRequestWithRedirectAndState(final URL url) {
        final URL tppRedirectUrl =
                new URL(redirectUrl)
                        .queryParam(QueryKeys.STATE, sessionStorage.get(StorageKeys.STATE));

        return createRequestWithoutRedirectHeader(url)
                .header(HeaderKeys.TPP_REDIRECT_URI, decodeUrl(tppRedirectUrl));
    }

    private String decodeUrl(final URL url) {
        try {
            return URLDecoder.decode(url.toString(), StandardCharsets.UTF_8.toString());
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalArgumentException(ErrorMessages.URL_ENCODING_ERROR);
        }
    }

    private RequestBuilder createRequest(final URL url) {
        return createRequestWithoutRedirectHeader(url)
                .header(HeaderKeys.TPP_REDIRECT_URI, redirectUrl);
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

    public boolean isConsentValid() {
        return persistentStorage
                .get(StorageKeys.CONSENT_ID, String.class)
                .map(consentId -> fetchConsentStatus(consentId).isValid())
                .orElse(false);
    }

    private ConsentStatusResponse fetchConsentStatus(String consentId) {
        return createRequestWithoutRedirectHeader(
                        new URL(DnbConstants.BASE_URL.concat(Urls.CONSENT_STATUS))
                                .parameter(IdTags.CONSENT_ID, consentId))
                .get(ConsentStatusResponse.class);
    }

    private ConsentResponse createConsent() {
        ConsentRequest consentsRequest = new ConsentRequest();
        return createRequestWithRedirectAndState(
                        new URL(DnbConstants.BASE_URL.concat(Urls.CONSENTS)))
                .post(ConsentResponse.class, consentsRequest);
    }

    private String getConsentId() {
        return persistentStorage
                .get(StorageKeys.CONSENT_ID, String.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }
}
