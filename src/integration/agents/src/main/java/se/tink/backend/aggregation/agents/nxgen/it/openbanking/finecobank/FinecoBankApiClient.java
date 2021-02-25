package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ParameterKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccountConsent;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentAuthorizationsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.PostConsentBodyRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.configuration.FinecoBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.card.rpc.CardAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.card.rpc.CardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.GetPaymentStatusResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class FinecoBankApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final String redirectUrl;
    private final String psuIpAddress;
    private final boolean requestManual;

    FinecoBankApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            AgentConfiguration<FinecoBankConfiguration> agentConfiguration,
            boolean requestManual,
            String psuIpAddress) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.psuIpAddress = psuIpAddress;
        this.requestManual = requestManual;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    public ConsentResponse getConsent(PostConsentBodyRequest postConsentBodyRequest, String state) {
        RequestBuilder requestBuilder =
                createRequest(Urls.CONSENTS)
                        .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                        .header(
                                HeaderKeys.TPP_REDIRECT_URI,
                                (new URL(redirectUrl).queryParam(QueryKeys.STATE, state)))
                        .body(postConsentBodyRequest);
        return addPsuIpAddressHeaderIfNeeded(requestBuilder).post(ConsentResponse.class);
    }

    public ConsentStatusResponse getConsentStatus() {
        RequestBuilder requestBuilder =
                createRequest(
                                Urls.CONSENT_STATUS.parameter(
                                        StorageKeys.CONSENT_ID,
                                        persistentStorage.get(StorageKeys.CONSENT_ID)))
                        .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString());
        return addPsuIpAddressHeaderIfNeeded(requestBuilder).get(ConsentStatusResponse.class);
    }

    public CardAccountsResponse fetchCreditCardAccounts() {
        RequestBuilder requestBuilder =
                createRequest(Urls.CARD_ACCOUNTS)
                        .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                        .header(
                                HeaderKeys.CONSENT_ID,
                                persistentStorage.get(StorageKeys.CONSENT_ID))
                        .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true));
        return addPsuIpAddressHeaderIfNeeded(requestBuilder).get(CardAccountsResponse.class);
    }

    public AccountsResponse fetchAccounts() {

        RequestBuilder requestBuilder =
                createRequest(Urls.ACCOUNTS)
                        .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                        .header(
                                HeaderKeys.CONSENT_ID,
                                persistentStorage.get(StorageKeys.CONSENT_ID))
                        .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true));
        return addPsuIpAddressHeaderIfNeeded(requestBuilder).get(AccountsResponse.class);
    }

    public PaginatorResponse getTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {

        SimpleDateFormat paginationDateFormatter =
                new SimpleDateFormat(Formats.DEFAULT_DATE_FORMAT);

        RequestBuilder requestBuilder =
                createRequest(
                                Urls.TRANSACTIONS.parameter(
                                        ParameterKeys.ACCOUNT_ID, account.getApiIdentifier()))
                        .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                        .header(
                                HeaderKeys.CONSENT_ID,
                                persistentStorage.get(StorageKeys.CONSENT_ID))
                        .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                        .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKED)
                        .queryParam(QueryKeys.DATE_FROM, paginationDateFormatter.format(fromDate))
                        .queryParam(QueryKeys.DATE_TO, paginationDateFormatter.format(toDate));
        return addPsuIpAddressHeaderIfNeeded(requestBuilder).get(TransactionsResponse.class);
    }

    public PaginatorResponse getCreditTransactions(CreditCardAccount account, LocalDate fromDate) {
        DateTimeFormatter paginationDateFormatter =
                DateTimeFormatter.ofPattern(Formats.DEFAULT_DATE_FORMAT);

        RequestBuilder requestBuilder =
                createRequest(
                                Urls.CARD_TRANSACTIONS.parameter(
                                        ParameterKeys.ACCOUNT_ID, account.getApiIdentifier()))
                        .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                        .header(
                                HeaderKeys.CONSENT_ID,
                                persistentStorage.get(StorageKeys.CONSENT_ID))
                        .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                        .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKED)
                        .queryParam(QueryKeys.DATE_FROM, paginationDateFormatter.format(fromDate));
        return addPsuIpAddressHeaderIfNeeded(requestBuilder).get(CardTransactionsResponse.class);
    }

    public ConsentAuthorizationsResponse getConsentAuthorizations() {
        RequestBuilder requestBuilder =
                createRequest(
                                Urls.CONSENT_AUTHORIZATIONS.parameter(
                                        StorageKeys.CONSENT_ID,
                                        persistentStorage.get(StorageKeys.CONSENT_ID)))
                        .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString());
        return addPsuIpAddressHeaderIfNeeded(requestBuilder)
                .get(ConsentAuthorizationsResponse.class);
    }

    public boolean isEmptyTransactionalAccountBalanceConsent() {
        List<AccountConsent> balancesItems = getBalancesConsentsFromStorage();

        return balancesItems.stream()
                .allMatch(balancesItem -> Strings.isNullOrEmpty(balancesItem.getIban()));
    }

    public boolean isEmptyCreditCardAccountBalanceConsent() {
        List<AccountConsent> balancesItems = getBalancesConsentsFromStorage();

        return balancesItems.stream()
                .allMatch(balancesItem -> Strings.isNullOrEmpty(balancesItem.getMaskedPan()));
    }

    private List<AccountConsent> getBalancesConsentsFromStorage() {
        return persistentStorage
                .get(StorageKeys.BALANCES_CONSENTS, new TypeReference<List<AccountConsent>>() {})
                .orElse(Collections.emptyList());
    }

    public CreatePaymentResponse createPayment(
            String paymentProduct, CreatePaymentRequest requestBody) {
        final String state = getStateFromStorage();
        final URL tppRedirectUrl = new URL(redirectUrl).queryParam(QueryKeys.STATE, state);

        RequestBuilder requestBuilder =
                client.request(
                                Urls.PAYMENT_INITIATION.parameter(
                                        ParameterKeys.PAYMENT_PRODUCT, paymentProduct))
                        .type(MediaType.APPLICATION_JSON)
                        .header(
                                HeaderKeys.X_REQUEST_ID,
                                HeaderValues.X_REQUEST_ID_PAYMENT_INITIATION)
                        .header(HeaderKeys.TPP_REDIRECT_URI, tppRedirectUrl.toString());
        return addPsuIpAddressHeaderIfNeeded(requestBuilder)
                .post(CreatePaymentResponse.class, requestBody);
    }

    public GetPaymentResponse getPayment(String paymentProduct, String paymentId) {
        return client.request(
                        Urls.GET_PAYMENT
                                .parameter(ParameterKeys.PAYMENT_PRODUCT, paymentProduct)
                                .parameter(ParameterKeys.PAYMENT_ID, paymentId))
                .header(HeaderKeys.X_REQUEST_ID, HeaderValues.X_REQUEST_ID_GET_PAYMENT)
                .get(GetPaymentResponse.class);
    }

    public GetPaymentStatusResponse getPaymentStatus(String paymentProduct, String paymentId) {
        return client.request(
                        Urls.GET_PAYMENT_STATUS
                                .parameter(ParameterKeys.PAYMENT_PRODUCT, paymentProduct)
                                .parameter(ParameterKeys.PAYMENT_ID, paymentId))
                .header(HeaderKeys.X_REQUEST_ID, HeaderValues.X_REQUEST_ID_GET_PAYMENT_STATUS)
                .get(GetPaymentStatusResponse.class);
    }

    private String getStateFromStorage() {
        return persistentStorage
                .get(StorageKeys.STATE, String.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.STATE_MISSING_ERROR));
    }

    private RequestBuilder addPsuIpAddressHeaderIfNeeded(RequestBuilder requestBuilder) {
        return requestManual
                ? requestBuilder.header(HeaderKeys.PSU_IP_ADDRESS, psuIpAddress)
                : requestBuilder;
    }
}
