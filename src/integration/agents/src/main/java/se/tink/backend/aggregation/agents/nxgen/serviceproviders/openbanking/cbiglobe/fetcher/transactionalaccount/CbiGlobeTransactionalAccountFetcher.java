package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.utls.CbiGlobeUtils.calculateFromDate;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CbiGlobeTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final CbiGlobeApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final CbiGlobeAuthenticationController controller;
    private static final Logger logger =
            LoggerFactory.getLogger(CbiGlobeTransactionalAccountFetcher.class);

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
        GetAccountsResponse getAccountsResponse =
                SerializationUtils.deserializeFromString(
                        persistentStorage.get(StorageKeys.ACCOUNTS), GetAccountsResponse.class);

        // only for testing, thiss will commit will be reverted after tests
        Collection<TransactionalAccount> accounts =
                getAccountsResponse.getAccounts().stream()
                        .map(acc -> acc.toTinkAccount(apiClient.getBalances(acc.getResourceId())))
                        .collect(Collectors.toList());
        logger.info("FETCHED ACCOUNTS: " + accounts.toString());
        return accounts;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        // Bank allows to fetch transactions for last 90 days
        fromDate = calculateFromDate(toDate);

        // only for testing, this commit will be reverted after tests
        PaginatorResponse transactions =
                apiClient.getTransactions(
                        account.getApiIdentifier(), fromDate, toDate, QueryValues.BOTH);
        logger.info("FETCHED TRANSACTIONS: " + transactions.toString());
        return transactions;
    }
}
