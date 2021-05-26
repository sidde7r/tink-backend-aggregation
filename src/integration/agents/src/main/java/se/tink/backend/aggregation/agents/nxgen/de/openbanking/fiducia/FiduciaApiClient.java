package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia;

import java.util.UUID;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.ErrorMessageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.QueryParamsKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.QueryParamsValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.entities.OtpCodeBody;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.entities.PsuData;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc.AuthorizeConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc.ScaResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc.ScaStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc.SelectScaMethodRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.rpc.AuthorizePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.rpc.PaymentDocument;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils.ErrorChecker;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@AllArgsConstructor
@Slf4j
public class FiduciaApiClient {

    private static final String CONSENTS_ENDPOINT = "/v1/consents";
    private static final String CONSENT_ENDPOINT = "/v1/consents/{consentId}";
    private static final String CONSENT_AUTHORIZATIONS_ENDPOINT =
            "/v1/consents/{consentId}/authorisations";
    private static final String ACCOUNTS_ENDPOINT = "/v1/accounts";
    private static final String BALANCES_ENDPOINT = "/v1/accounts/{accountId}/balances";
    private static final String TRANSACTIONS_ENDPOINT = "/v1/accounts/{accountId}/transactions";
    private static final String PAYMENTS_ENDPOINT = "/v1/payments/pain.001-sepa-credit-transfers";
    private static final String PAYMENT_AUTHORIZATION_ENDPOINT =
            "/v1/payments/pain.001-sepa-credit-transfers/{paymentId}/authorisations";
    private static final String PAYMENT_ENDPOINT =
            "/v1/payments/pain.001-sepa-credit-transfers/{paymentId}";

    private static final String ACCOUNT_ID = "accountId";
    private static final String CONSENT_ID = "consentId";
    private static final String PAYMENT_ID = "paymentId";

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final String userIp;
    private final String serverUrl;

    private URL createUrl(String path) {
        return new URL(serverUrl + "/bg13" + path);
    }

    public RequestBuilder createRequestInSession(URL url, String consentId) {
        return createRequest(url).header(FiduciaConstants.HeaderKeys.CONSENT_ID, consentId);
    }

    public RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(FiduciaConstants.HeaderKeys.ACCEPT, MediaType.APPLICATION_JSON)
                .header(FiduciaConstants.HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(FiduciaConstants.HeaderKeys.PSU_IP_ADDRESS, userIp);
    }

    public String createConsent(String username) {
        ConsentRequest createConsentRequest =
                new ConsentRequest(
                        new AccessEntity("allAccounts"),
                        true,
                        FormValues.VALID_UNTIL,
                        FormValues.FREQUENCY_PER_DAY,
                        false);

        try {
            return createRequest(createUrl(CONSENTS_ENDPOINT))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .header(StorageKeys.PSU_ID, username)
                    .post(ConsentResponse.class, createConsentRequest)
                    .getConsentId();
        } catch (HttpResponseException e) {
            throw ErrorChecker.errorChecker(e);
        }
    }

    public ConsentDetailsResponse getConsentDetails(String consentId) {
        return createRequest(createUrl(CONSENT_ENDPOINT).parameter(CONSENT_ID, consentId))
                .get(ConsentDetailsResponse.class);
    }

    public ScaResponse authorizeConsent(String consentId, String password) {
        AuthorizeConsentRequest authorizeConsentRequest =
                new AuthorizeConsentRequest(new PsuData(password));

        try {
            return createRequest(
                            createUrl(CONSENT_AUTHORIZATIONS_ENDPOINT)
                                    .parameter(CONSENT_ID, consentId))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(ScaResponse.class, authorizeConsentRequest);
        } catch (HttpResponseException e) {
            throw ErrorChecker.errorChecker(e);
        }
    }

    public ScaResponse selectAuthMethod(String urlPath, String scaMethodId) {
        SelectScaMethodRequest request = new SelectScaMethodRequest(scaMethodId);

        try {
            return createRequest(createUrl(urlPath))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .put(ScaResponse.class, request);
        } catch (HttpResponseException e) {
            throw ErrorChecker.errorChecker(e);
        }
    }

    public ScaStatusResponse authorizeWithOtpCode(String urlPath, String otpCode) {
        OtpCodeBody otpCodeBody = new OtpCodeBody(otpCode);
        try {
            return createRequest(createUrl(urlPath))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .put(ScaStatusResponse.class, otpCodeBody);
        } catch (HttpResponseException e) {
            if (e.getResponse()
                    .getBody(String.class)
                    .contains(ErrorMessageKeys.PSU_CREDENTIALS_INVALID)) {
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
            }
            throw e;
        }
    }

    public GetAccountsResponse getAccounts() {
        return createRequestInSession(
                        createUrl(ACCOUNTS_ENDPOINT), persistentStorage.get(StorageKeys.CONSENT_ID))
                .get(GetAccountsResponse.class);
    }

    public GetBalancesResponse getBalances(String accountId) {
        return createRequestInSession(
                        createUrl(BALANCES_ENDPOINT).parameter(ACCOUNT_ID, accountId),
                        persistentStorage.get(StorageKeys.CONSENT_ID))
                .get(GetBalancesResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> getTransactions(TransactionalAccount account) {
        URL url =
                createUrl(TRANSACTIONS_ENDPOINT).parameter(ACCOUNT_ID, account.getApiIdentifier());
        String consentId = persistentStorage.get(StorageKeys.CONSENT_ID);
        return createRequestInSession(url, consentId)
                .queryParam(QueryParamsKeys.BOOKING_STATUS, QueryParamsValues.BOOKING_STATUS)
                .queryParam(QueryParamsKeys.DATE_FROM, QueryParamsValues.DATE_FROM)
                .get(GetTransactionsResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> getTransactions(String continuationPath) {
        String consentId = persistentStorage.get(StorageKeys.CONSENT_ID);
        return createRequestInSession(createUrl(continuationPath), consentId)
                .get(GetTransactionsResponse.class);
    }

    public CreatePaymentResponse createPayment(String body, String psuId) {
        return createRequest(createUrl(PAYMENTS_ENDPOINT))
                .header(HeaderKeys.PSU_ID, psuId)
                .type(MediaType.APPLICATION_XML_TYPE)
                .post(CreatePaymentResponse.class, body);
    }

    public AuthorizePaymentResponse authorizePayment(String paymentId, String body, String psuId) {
        return createRequest(
                        createUrl(PAYMENT_AUTHORIZATION_ENDPOINT).parameter(PAYMENT_ID, paymentId))
                .header(HeaderKeys.PSU_ID, psuId)
                .post(AuthorizePaymentResponse.class, body);
    }

    public PaymentDocument getPayment(String paymentId) {
        return createRequest(createUrl(PAYMENT_ENDPOINT).parameter(PAYMENT_ID, paymentId))
                .accept(MediaType.APPLICATION_XML)
                .get(PaymentDocument.class);
    }
}
