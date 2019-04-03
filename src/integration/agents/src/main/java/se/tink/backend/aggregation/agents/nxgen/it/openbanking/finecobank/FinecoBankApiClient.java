package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.configuration.FinecoBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
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

    public FinecoBankApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
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

    public AccountsResponse fetchAccounts() {

        return createRequest(Urls.ACCOUNTS)
                .header(
                        FinecoBankConstants.HeaderKeys.X_REQUEST_ID,
                        FinecoBankConstants.HeaderValues.X_REQUEST_ID_ACCOUNTS)
                .header(
                        FinecoBankConstants.HeaderKeys.CONSENT_ID,
                        FinecoBankConstants.HeaderValues.CONSENT_ID)
                .queryParam(FinecoBankConstants.QueryKeys.WITH_BALANCE, String.valueOf(true))
                .get(AccountsResponse.class);
    }

    public PaginatorResponse getTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {

        SimpleDateFormat paginationDateFormatter =
                new SimpleDateFormat(Formats.PAGINATION_DATE_FORMAT);

        return createRequest(
                        Urls.TRANSACTIONS.parameter(
                                FinecoBankConstants.ParameterKeys.ACCOUNT_ID,
                                account.getApiIdentifier()))
                .header(
                        FinecoBankConstants.HeaderKeys.X_REQUEST_ID,
                        FinecoBankConstants.HeaderValues.X_REQUEST_ID_TRANSACTIONS)
                .header(
                        FinecoBankConstants.HeaderKeys.CONSENT_ID,
                        FinecoBankConstants.HeaderValues.CONSENT_ID)
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
}
