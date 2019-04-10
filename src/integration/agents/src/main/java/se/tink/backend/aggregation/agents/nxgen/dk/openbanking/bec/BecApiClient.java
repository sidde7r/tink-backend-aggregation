package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.BecConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.BecConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.BecConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.BecConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.BecConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.BecConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.configuration.BecConfiguration;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class BecApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private BecConfiguration configuration;

    public BecApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    public BecConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(BecConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        final String clientId = getConfiguration().getClientId();

        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_IBM_CLIENT_ID, clientId);
    }

    public List<TransactionalAccount> getAccounts() {
        return createRequest(Urls.GET_ACCOUNTS)
                .queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                .get(GetAccountsResponse.class)
                .toTinkAccounts();
    }

    public PaginatorResponse getTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {
        final URL url =
                Urls.GET_TRANSACTIONS.parameter(IdTags.ACCOUNT_ID, account.getApiIdentifier());

        return createRequest(url)
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .get(GetTransactionsResponse.class);
    }
}
