package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank;

import com.fasterxml.jackson.core.type.TypeReference;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.BalancesItem;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentAuthorizationsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.PostConsentBodyRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.configuration.FinecoBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.cards.rpc.CardAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.cards.rpc.CardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class FinecoBankApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private FinecoBankConfiguration configuration;

    public FinecoBankApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            FinecoBankConfiguration finecoBankConfiguration) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.configuration = finecoBankConfiguration;
    }

    private FinecoBankConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(FinecoBankConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromStorage();

        return createRequest(url).addBearerToken(authToken);
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public ConsentResponse getConsent(PostConsentBodyRequest postConsentBodyRequest, String state) {
        return createRequest(Urls.CONSENTS)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        (new URL(getConfiguration().getRedirectUrl())
                                .queryParam(QueryKeys.STATE, state)))
                .header(HeaderKeys.PSU_IP_ADDRESS, getConfiguration().getPsuIpAddress())
                .body(postConsentBodyRequest)
                .post(ConsentResponse.class);
    }

    public ConsentStatusResponse getConsentStatus() {
        return createRequest(
                        Urls.CONSENT_STATUS.parameter(
                                StorageKeys.CONSENT_ID,
                                persistentStorage.get(StorageKeys.CONSENT_ID)))
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_IP_ADDRESS, getConfiguration().getPsuIpAddress())
                .get(ConsentStatusResponse.class);
    }

    public CardAccountsResponse fetchCreditCardAccounts() {
        return createRequest(Urls.CARD_ACCOUNTS)
                .header(FinecoBankConstants.HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(
                        FinecoBankConstants.HeaderKeys.CONSENT_ID,
                        persistentStorage.get(StorageKeys.CONSENT_ID))
                .queryParam(FinecoBankConstants.QueryKeys.WITH_BALANCE, String.valueOf(true))
                .header(HeaderKeys.PSU_IP_ADDRESS, getConfiguration().getPsuIpAddress())
                .get(CardAccountsResponse.class);
    }

    public AccountsResponse fetchAccounts() {

        return createRequest(Urls.ACCOUNTS)
                .header(FinecoBankConstants.HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(
                        FinecoBankConstants.HeaderKeys.CONSENT_ID,
                        persistentStorage.get(StorageKeys.CONSENT_ID))
                .queryParam(FinecoBankConstants.QueryKeys.WITH_BALANCE, String.valueOf(true))
                .header(HeaderKeys.PSU_IP_ADDRESS, getConfiguration().getPsuIpAddress())
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
                .header(HeaderKeys.PSU_IP_ADDRESS, getConfiguration().getPsuIpAddress())
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
                .header(HeaderKeys.PSU_IP_ADDRESS, getConfiguration().getPsuIpAddress())
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
                .header(HeaderKeys.PSU_IP_ADDRESS, getConfiguration().getPsuIpAddress())
                .get(ConsentAuthorizationsResponse.class);
    }

    public boolean isEmptyBalanceConsent() {
        List<BalancesItem> balancesItems =
                persistentStorage
                        .get(
                                StorageKeys.BALANCE_ACCOUNTS,
                                new TypeReference<List<BalancesItem>>() {})
                        .orElse(Collections.emptyList());
        return balancesItems.isEmpty();
    }
}
