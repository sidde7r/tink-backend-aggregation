package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc.AuthorizeConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc.CreateConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc.CreateConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.configuration.FiduciaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.rpc.AuthorizePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.rpc.PaymentDocument;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class FiduciaApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private FiduciaConfiguration configuration;

    public FiduciaApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    private FiduciaConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(FiduciaConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(
            URL url,
            String reqId,
            String digest,
            String signature,
            String certificate,
            String date) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, reqId)
                .header(HeaderKeys.DIGEST, digest)
                .header(HeaderKeys.SIGNATURE, signature)
                .header(HeaderKeys.TPP_SIGNATURE_CERTIFICATE, certificate)
                .header(HeaderKeys.DATE, date);
    }

    private RequestBuilder createRequestInSession(
            URL url,
            String reqId,
            String digest,
            String signature,
            String certificate,
            String date) {
        // Consent id is mocked to work only with their default value
        return createRequest(url, reqId, digest, signature, certificate, date)
                .header(HeaderKeys.CONSENT_ID, HeaderValues.CONSENT_VALID);
    }

    public CreateConsentResponse createConsent(
            CreateConsentRequest body,
            String digest,
            String certificate,
            String signature,
            String reqId,
            String date,
            String psuId) {
        return createRequest(Urls.CREATE_CONSENT, reqId, digest, signature, certificate, date)
                .header(HeaderKeys.PSU_ID, psuId)
                .post(CreateConsentResponse.class, body);
    }

    public void authorizeConsent(
            CreateConsentResponse createConsentResponse,
            String digest,
            String certificate,
            String signature,
            String reqId,
            String date,
            AuthorizeConsentRequest body) {
        createRequest(
                        Urls.AUTHORIZE_CONSENT.parameter(
                                IdTags.CONSENT_ID, createConsentResponse.getConsentId()),
                        reqId,
                        digest,
                        signature,
                        certificate,
                        date)
                .post(HttpResponse.class, body);
    }

    public GetAccountsResponse getAccounts(
            String digest, String certificate, String signature, String reqId, String date) {
        return createRequestInSession(
                        Urls.GET_ACCOUNTS, reqId, digest, signature, certificate, date)
                .get(GetAccountsResponse.class);
    }

    public GetBalancesResponse getBalances(
            AccountEntity acc,
            String digest,
            String certificate,
            String signature,
            String reqId,
            String date) {
        return createRequestInSession(
                        Urls.GET_BALANCES.parameter(IdTags.ACCOUNT_ID, acc.getResourceId()),
                        reqId,
                        digest,
                        signature,
                        certificate,
                        date)
                .get(GetBalancesResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> getTransactions(
            TransactionalAccount account,
            String key,
            String digest,
            String certificate,
            String signature,
            String reqId,
            String date) {
        URL url =
                Optional.ofNullable(key)
                        .map(k -> new URL(Urls.BASE_URL + k))
                        .orElse(
                                Urls.GET_TRANSACTIONS.parameter(
                                        IdTags.ACCOUNT_ID, account.getApiIdentifier()));

        return createRequestInSession(url, reqId, digest, signature, certificate, date)
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKED)
                .get(GetTransactionsResponse.class);
    }

    public CreatePaymentResponse createPayment(
            String body,
            String psuId,
            String digest,
            String certificate,
            String signature,
            String reqId,
            String date) {
        return createRequest(Urls.CREATE_PAYMENT, reqId, digest, signature, certificate, date)
                .header(HeaderKeys.PSU_ID, psuId)
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                .type(MediaType.APPLICATION_XML_TYPE)
                .post(CreatePaymentResponse.class, body);
    }

    public AuthorizePaymentResponse authorizePayment(
            String paymentId,
            String body,
            String psuId,
            String digest,
            String certificate,
            String signature,
            String reqId,
            String date) {
        return createRequest(
                        Urls.AUTHORIZE_PAYMENT.parameter(IdTags.PAYMENT_ID, paymentId),
                        reqId,
                        digest,
                        signature,
                        certificate,
                        date)
                .header(HeaderKeys.PSU_ID, psuId)
                .post(AuthorizePaymentResponse.class, body);
    }

    public PaymentDocument getPayment(
            String paymentId,
            String digest,
            String certificate,
            String signature,
            String reqId,
            String date) {
        return createRequest(
                        Urls.GET_PAYMENT.parameter(IdTags.PAYMENT_ID, paymentId),
                        reqId,
                        digest,
                        signature,
                        certificate,
                        date)
                .accept(MediaType.APPLICATION_XML)
                .get(PaymentDocument.class);
    }
}
