package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.Configuration;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ParameterKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccountConsent;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentAuthorizationsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.PostConsentBodyRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.configuration.FinecoBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.cards.rpc.CardAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.cards.rpc.CardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.executor.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.executor.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.executor.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.executor.rpc.GetPaymentStatusResponse;
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

    public FinecoBankApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            AgentConfiguration<FinecoBankConfiguration> agentConfiguration) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    private String getRedirectUrl() {
        return Optional.ofNullable(redirectUrl)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    public ConsentResponse getConsent(PostConsentBodyRequest postConsentBodyRequest, String state) {
        return createRequest(Urls.CONSENTS)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        (new URL(getRedirectUrl()).queryParam(QueryKeys.STATE, state)))
                .header(HeaderKeys.PSU_IP_ADDRESS, Configuration.PSU_IP_ADDRESS)
                .body(postConsentBodyRequest)
                .post(ConsentResponse.class);
    }

    public ConsentStatusResponse getConsentStatus() {
        return createRequest(
                        Urls.CONSENT_STATUS.parameter(
                                StorageKeys.CONSENT_ID,
                                persistentStorage.get(StorageKeys.CONSENT_ID)))
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_IP_ADDRESS, Configuration.PSU_IP_ADDRESS)
                .get(ConsentStatusResponse.class);
    }

    public CardAccountsResponse fetchCreditCardAccounts() {
        return createRequest(Urls.CARD_ACCOUNTS)
                .header(FinecoBankConstants.HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(
                        FinecoBankConstants.HeaderKeys.CONSENT_ID,
                        persistentStorage.get(StorageKeys.CONSENT_ID))
                .queryParam(FinecoBankConstants.QueryKeys.WITH_BALANCE, String.valueOf(true))
                .header(HeaderKeys.PSU_IP_ADDRESS, Configuration.PSU_IP_ADDRESS)
                .get(CardAccountsResponse.class);
    }

    public AccountsResponse fetchAccounts() {

        return createRequest(Urls.ACCOUNTS)
                .header(FinecoBankConstants.HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(
                        FinecoBankConstants.HeaderKeys.CONSENT_ID,
                        persistentStorage.get(StorageKeys.CONSENT_ID))
                .queryParam(FinecoBankConstants.QueryKeys.WITH_BALANCE, String.valueOf(true))
                .header(HeaderKeys.PSU_IP_ADDRESS, Configuration.PSU_IP_ADDRESS)
                .get(AccountsResponse.class);
    }

    public PaginatorResponse getTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {

        SimpleDateFormat paginationDateFormatter =
                new SimpleDateFormat(Formats.DEFAULT_DATE_FORMAT);

        return createRequest(
                        Urls.TRANSACTIONS.parameter(
                                FinecoBankConstants.ParameterKeys.ACCOUNT_ID,
                                account.getApiIdentifier()))
                .header(FinecoBankConstants.HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_IP_ADDRESS, Configuration.PSU_IP_ADDRESS)
                .header(
                        FinecoBankConstants.HeaderKeys.CONSENT_ID,
                        persistentStorage.get(StorageKeys.CONSENT_ID))
                .queryParam(FinecoBankConstants.QueryKeys.WITH_BALANCE, String.valueOf(true))
                .queryParam(
                        FinecoBankConstants.QueryKeys.BOOKING_STATUS,
                        FinecoBankConstants.QueryValues.BOOKED)
                .queryParam(
                        FinecoBankConstants.QueryKeys.DATE_FROM,
                        paginationDateFormatter.format(fromDate))
                .queryParam(
                        FinecoBankConstants.QueryKeys.DATE_TO,
                        paginationDateFormatter.format(toDate))
                .get(TransactionsResponse.class);
    }

    public PaginatorResponse getCreditTransactions(
            CreditCardAccount account, Date fromDate, Date toDate) {
        SimpleDateFormat paginationDateFormatter =
                new SimpleDateFormat(Formats.DEFAULT_DATE_FORMAT);

        return createRequest(
                        Urls.CARD_TRANSACTIONS.parameter(
                                FinecoBankConstants.ParameterKeys.ACCOUNT_ID,
                                account.getApiIdentifier()))
                .header(FinecoBankConstants.HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_IP_ADDRESS, Configuration.PSU_IP_ADDRESS)
                .header(
                        FinecoBankConstants.HeaderKeys.CONSENT_ID,
                        persistentStorage.get(StorageKeys.CONSENT_ID))
                .queryParam(FinecoBankConstants.QueryKeys.WITH_BALANCE, String.valueOf(true))
                .queryParam(
                        FinecoBankConstants.QueryKeys.BOOKING_STATUS,
                        FinecoBankConstants.QueryValues.BOOKED)
                .queryParam(
                        FinecoBankConstants.QueryKeys.DATE_FROM,
                        paginationDateFormatter.format(fromDate))
                .queryParam(
                        FinecoBankConstants.QueryKeys.DATE_TO,
                        paginationDateFormatter.format(toDate))
                .get(CardTransactionsResponse.class);
    }

    public ConsentAuthorizationsResponse getConsentAuthorizations() {
        return createRequest(
                        Urls.CONSENT_AUTHORIZATIONS.parameter(
                                StorageKeys.CONSENT_ID,
                                persistentStorage.get(StorageKeys.CONSENT_ID)))
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_IP_ADDRESS, Configuration.PSU_IP_ADDRESS)
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
                .get(StorageKeys.BALANCE_ACCOUNTS, new TypeReference<List<AccountConsent>>() {})
                .orElse(Collections.emptyList());
    }

    public CreatePaymentResponse createPayment(
            String paymentProduct, CreatePaymentRequest requestBody) {
        final String state = getStateFromStorage();
        final URL tppRedirectUrl = new URL(getRedirectUrl()).queryParam(QueryKeys.STATE, state);

        return client.request(
                        Urls.PAYMENT_INITIATION.parameter(
                                ParameterKeys.PAYMENT_PRODUCT, paymentProduct))
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, HeaderValues.X_REQUEST_ID_PAYMENT_INITIATION)
                .header(HeaderKeys.PSU_IP_ADDRESS, Configuration.PSU_IP_ADDRESS)
                .header(HeaderKeys.TPP_REDIRECT_URI, tppRedirectUrl.toString())
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

    public String getStateFromStorage() {
        return persistentStorage
                .get(StorageKeys.STATE, String.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.STATE_MISSING_ERROR));
    }
}
