package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher;

import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.TargoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.TargoBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.rpc.TransactionSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.utils.TargoBankUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class TargoBankTransactionsFetcher implements TransactionFetcher<TransactionalAccount> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TargoBankTransactionsFetcher.class);
    private final TargoBankApiClient apiClient;

    private TargoBankTransactionsFetcher(TargoBankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static TargoBankTransactionsFetcher create(TargoBankApiClient apiClient) {
        return new TargoBankTransactionsFetcher(apiClient);
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        String webId = account.getTemporaryStorage().get(TargoBankConstants.Tags.WEB_ID);
        Optional<TransactionSummaryResponse> transactionsForAccount = getTransactionsForAccount(webId);

        List<AggregationTransaction> transactions = Lists.newArrayList();
        transactionsForAccount.ifPresent(transactionList ->
                transactionList.getTransactions().stream()
                        .map(TransactionEntity::toTransaction)
                        .forEach(transactions::add)
        );
        return transactions;
    }

    private Optional<TransactionSummaryResponse> getTransactionsForAccount(String webId) {
        TransactionSummaryResponse details = apiClient.getTransactions(webId);
        if (!TargoBankUtils.isSuccess(details.getReturnCode())) {
            return Optional.empty();
        }
        return Optional.of(details);
    }

}
