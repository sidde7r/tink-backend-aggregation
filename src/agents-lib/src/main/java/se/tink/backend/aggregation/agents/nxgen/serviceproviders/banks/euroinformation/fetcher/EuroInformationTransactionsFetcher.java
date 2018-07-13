package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher;

import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.TransactionSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class EuroInformationTransactionsFetcher implements TransactionFetcher<TransactionalAccount> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EuroInformationTransactionsFetcher.class);
    private final EuroInformationApiClient apiClient;

    private EuroInformationTransactionsFetcher(EuroInformationApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static EuroInformationTransactionsFetcher create(EuroInformationApiClient apiClient) {
        return new EuroInformationTransactionsFetcher(apiClient);
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        // TODO: Temporary fix for double serialization problem, please remove `replace` method when fixed
        String webId = account.getTemporaryStorage().get(EuroInformationConstants.Tags.WEB_ID).replace("\"", "");
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
        if (!EuroInformationUtils.isSuccess(details.getReturnCode())) {
            return Optional.empty();
        }
        return Optional.of(details);
    }

}
