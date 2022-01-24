package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.rpc.ScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.configuration.SparebankApiConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.enums.SparebankPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.PaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.rpc.CardResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.rpc.CardTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.filters.ConsentErrorsFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.utils.SparebankUtils;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.cryptography.hash.Hash;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
public class SparebankApiClient {

    private final TinkHttpClient client;
    private final QsealcSigner signer;
    private final SparebankApiConfiguration apiConfiguration;
    @Getter private final SparebankStorage storage;

    public SparebankApiClient(
            TinkHttpClient client,
            QsealcSigner signer,
            SparebankApiConfiguration apiConfiguration,
            SparebankStorage storage) {
        this.client = client;
        this.signer = signer;
        this.apiConfiguration = apiConfiguration;
        this.storage = storage;

        this.client.addFilter(new ConsentErrorsFilter());
    }

    private RequestBuilder createRequest(URL url) {
        return createRequest(url, Optional.empty());
    }

    private RequestBuilder createRequest(URL url, Optional<String> digest) {
        Map<String, Object> headers = getHeaders(digest);
        headers.put(
                HeaderKeys.SIGNATURE,
                SparebankUtils.generateSignatureHeader(apiConfiguration, signer, headers));
        return client.request(url).headers(headers);
    }

    public ScaResponse getScaRedirect(String state) throws HttpResponseException {
        storage.storeState(state);
        return createRequest(new URL(apiConfiguration.getBaseUrl() + Urls.GET_SCA_REDIRECT))
                .header(HeaderKeys.PSU_CONTEXT, HeaderValues.PSU_CONTEXT_PRIVATE)
                .post(ScaResponse.class);
    }

    public AccountResponse fetchAccounts() {
        Optional<AccountResponse> maybeAccounts = storage.getStoredAccounts();
        if (maybeAccounts.isPresent()) {
            return maybeAccounts.get();
        }
        AccountResponse accountResponse =
                createRequest(new URL(apiConfiguration.getBaseUrl() + Urls.GET_ACCOUNTS))
                        .queryParam(QueryKeys.WITH_BALANCE, "false")
                        .header(HeaderKeys.X_ACCEPT_FIX, HeaderValues.X_ACCEPT_FIX_LONGER_NAMES)
                        .get(AccountResponse.class);
        storage.storeAccounts(accountResponse);
        return accountResponse;
    }

    public BalanceResponse fetchBalances(String resourceId) {
        Optional<BalanceResponse> maybeBalanceResponse =
                storage.getStoredBalanceResponse(resourceId);
        if (maybeBalanceResponse.isPresent()) {
            storage.removeBalanceResponseFromStorage(resourceId);
            return maybeBalanceResponse.get();
        }
        return createRequest(
                        new URL(apiConfiguration.getBaseUrl() + Urls.FETCH_BALANCES)
                                .parameter(IdTags.RESOURCE_ID, resourceId))
                .get(BalanceResponse.class);
    }

    public TransactionResponse fetchTransactions(String resourceId) {
        return fetchTransactions(Urls.FETCH_TRANSACTIONS, resourceId)
                .get(TransactionResponse.class);
    }

    public TransactionResponse fetchNextTransactions(String path) {
        return createRequest(new URL(apiConfiguration.getBaseUrl() + path))
                .get(TransactionResponse.class);
    }

    public CardResponse fetchCards() {
        Optional<CardResponse> maybeCards = storage.getStoredCards();
        if (maybeCards.isPresent()) {
            return maybeCards.get();
        }

        try {
            CardResponse cardResponse =
                    createRequest(new URL(apiConfiguration.getBaseUrl() + Urls.GET_CARDS))
                            .header(HeaderKeys.X_ACCEPT_FIX, HeaderValues.X_ACCEPT_FIX_LONGER_NAMES)
                            .get(CardResponse.class);
            storage.storeCards(cardResponse);
            return cardResponse;

        } catch (HttpResponseException e) {
            if (bankDoesNotSupportCreditCards(e)) {
                log.warn("[Sparebank][CC] Bank does not support CC endpoint");
                return CardResponse.empty();
            }
            throw e;
        }
    }

    private boolean bankDoesNotSupportCreditCards(HttpResponseException e) {
        return HttpStatus.SC_NOT_FOUND == e.getResponse().getStatus();
    }

    public BalanceResponse fetchCardBalances(String resourceId) {
        Optional<BalanceResponse> maybeBalanceResponse =
                storage.getStoredBalanceResponse(resourceId);
        if (maybeBalanceResponse.isPresent()) {
            storage.removeBalanceResponseFromStorage(resourceId);
            return maybeBalanceResponse.get();
        }
        return createRequest(
                        new URL(apiConfiguration.getBaseUrl() + Urls.GET_CARD_BALANCES)
                                .parameter(IdTags.RESOURCE_ID, resourceId))
                .get(BalanceResponse.class);
    }

    public CardTransactionResponse fetchCardTransactions(String resourceId) {
        return fetchTransactions(Urls.GET_CARD_TRANSACTIONS, resourceId)
                .header(HeaderKeys.X_ACCEPT_FIX, HeaderValues.X_ACCEPT_FIX_AMOUNT_SWITCH)
                .get(CardTransactionResponse.class);
    }

    public CardTransactionResponse fetchNextCardTransactions(String path) {
        return createRequest(new URL(apiConfiguration.getBaseUrl() + path))
                .get(CardTransactionResponse.class);
    }

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest paymentRequest, SparebankPaymentType paymentType) {

        return createRequest(
                        new URL(apiConfiguration.getBaseUrl().concat(Urls.CREATE_PAYMENT))
                                .parameter(IdTags.SERVICE_TYPE, paymentType.getTypePath())
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentType.getSubtypePath()),
                        createDigest(SerializationUtils.serializeToString(paymentRequest)))
                .header(HeaderKeys.X_ACCEPT_FIX, HeaderValues.AMOUNT_AS_STRING)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body(paymentRequest)
                .post(CreatePaymentResponse.class);
    }

    public GetPaymentResponse fetchPayment(String getPaymentUrl) {
        return createRequest(new URL(apiConfiguration.getBaseUrl() + new URL(getPaymentUrl)))
                .header(HeaderKeys.X_ACCEPT_FIX, HeaderValues.AMOUNT_AS_STRING)
                .get(GetPaymentResponse.class);
    }

    public PaymentStatusResponse fetchPaymentStatus(String paymentStatusUrl) {
        return createRequest(new URL(apiConfiguration.getBaseUrl() + new URL(paymentStatusUrl)))
                .header(HeaderKeys.X_ACCEPT_FIX, HeaderValues.AMOUNT_AS_STRING)
                .get(PaymentStatusResponse.class);
    }

    public PaymentStatusResponse authorizePayment(String paymentAuthorizeUrl) {

        return createRequest(new URL(apiConfiguration.getBaseUrl() + paymentAuthorizeUrl))
                .header(HeaderKeys.X_ACCEPT_FIX, HeaderValues.AMOUNT_AS_STRING)
                .post(PaymentStatusResponse.class);
    }

    public PaymentStatusResponse cancelPayment(String paymentId, SparebankPaymentType paymentType) {
        return createRequest(
                        new URL(apiConfiguration.getBaseUrl() + Urls.GET_PAYMENT)
                                .parameter(IdTags.SERVICE_TYPE, paymentType.getTypePath())
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentType.getSubtypePath())
                                .parameter(IdTags.PAYMENT_ID, paymentId))
                .header(HeaderKeys.X_ACCEPT_FIX, HeaderValues.AMOUNT_AS_STRING)
                .delete(PaymentStatusResponse.class);
    }

    private RequestBuilder fetchTransactions(String transactionUrl, String resourceId) {
        LocalDate fromDate;
        if (storage.isStoredConsentTooOldForFullFetch()) {
            log.info("Fetching transactions from last 90 days");
            fromDate = LocalDate.now().minusDays(89);
        } else {
            log.info("Fetching all transactions");
            fromDate = LocalDate.of(1970, 1, 1);
        }
        LocalDate toDate = LocalDate.now();

        return createRequest(
                        new URL(apiConfiguration.getBaseUrl() + transactionUrl)
                                .parameter(IdTags.RESOURCE_ID, resourceId))
                .queryParam(
                        SparebankConstants.QueryKeys.DATE_FROM,
                        fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .queryParam(
                        SparebankConstants.QueryKeys.DATE_TO,
                        toDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .queryParam(
                        SparebankConstants.QueryKeys.LIMIT,
                        SparebankConstants.QueryValues.TRANSACTION_LIMIT)
                .queryParam(
                        SparebankConstants.QueryKeys.BOOKING_STATUS,
                        SparebankConstants.QueryValues.BOOKING_STATUS);
    }

    private Map<String, Object> getHeaders(Optional<String> digest) {
        String tppRedirectUrl =
                new URL(apiConfiguration.getRedirectUrl())
                        .queryParam(QueryKeys.STATE, storage.getState())
                        .toString();

        Map<String, Object> headers = new HashMap<>();
        headers.put(HeaderKeys.ACCEPT, MediaType.APPLICATION_JSON);
        headers.put(
                HeaderKeys.DATE, ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        headers.put(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId());
        headers.put(HeaderKeys.TPP_REDIRECT_URI, tppRedirectUrl);
        headers.put(HeaderKeys.TPP_SIGNATURE_CERTIFICATE, apiConfiguration.getQsealcBase64());

        // if this condition is correct is currently a mistery while auto auth is not implemented.
        // Added info about that in ticket ITE-1648
        if (apiConfiguration.isUserPresent()) {
            headers.put(HeaderKeys.PSU_IP_ADDRESS, apiConfiguration.getUserIp());
        }

        digest.ifPresent(digestString -> headers.put(HeaderKeys.DIGEST, digestString));
        storage.getSessionId()
                .ifPresent(sessionId -> headers.put(HeaderKeys.TPP_SESSION_ID, sessionId));
        storage.getPsuId().ifPresent(psuId -> headers.put(HeaderKeys.PSU_ID, psuId));

        return headers;
    }

    private Optional<String> createDigest(final String data) {
        return Optional.of(
                String.format(
                        SparebankConstants.HeaderValues.SHA_256.concat("%s"),
                        Base64.getEncoder().encodeToString(Hash.sha256(data))));
    }
}
