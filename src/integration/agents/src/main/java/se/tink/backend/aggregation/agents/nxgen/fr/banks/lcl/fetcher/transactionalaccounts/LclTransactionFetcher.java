package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.LclApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.LclConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.entities.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class LclTransactionFetcher implements TransactionFetcher<TransactionalAccount> {

    private final LclApiClient apiClient;

    public LclTransactionFetcher(LclApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * LCL don't support pagination in the app or on the web, right now we get around one month of transactions.
     * The user can get more history by going through pdf documents, but for now we decided not to implement pdf
     * parsing for this purpose.
     */
    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        AccountDetailsEntity accountDetailsEntity = account
                .getFromTemporaryStorage(LclConstants.Storage.ACCOUNT_DETAILS_ENTITY, AccountDetailsEntity.class)
                .orElseThrow(() -> new IllegalStateException("No account details entity found."));

        TransactionsResponse transactionsResponse = apiClient.getTransactions(accountDetailsEntity);

        return Optional.ofNullable(transactionsResponse.getTransactionsList())
                .orElse(Collections.emptyList())
                .stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
        }
}
