package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CbiGlobeTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    protected final CbiGlobeApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final CbiGlobeAuthenticationController controller;

    public CbiGlobeTransactionalAccountFetcher(
            CbiGlobeApiClient apiClient,
            PersistentStorage persistentStorage,
            CbiGlobeAuthenticationController controller) {
        this.apiClient = apiClient;
        this.controller = controller;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        GetAccountsResponse getAccountsResponse = apiClient.getAccounts();
        controller.openThirdPartyApp(getAccountsResponse);

        return getAccountsResponse.getAccounts().stream()
                .map(acc -> acc.toTinkAccount(apiClient.getBalances(acc.getResourceId())))
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        // Bank allows to fetch transactions for last 90 days
        fromDate = calculateFromDate(toDate);
        return apiClient.getTransactions(
                account.getApiIdentifier(), fromDate, toDate, QueryValues.BOTH);
    }

    protected Date calculateFromDate(Date toDate) {
        return new DateTime(toDate).minusDays(90).toDate();
    }
}
