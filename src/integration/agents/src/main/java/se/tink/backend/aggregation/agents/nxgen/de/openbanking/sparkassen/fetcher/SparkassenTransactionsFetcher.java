package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.detail.TransactionMapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class SparkassenTransactionsFetcher implements TransactionFetcher<TransactionalAccount> {
    private final SparkassenApiClient apiClient;
    private final SparkassenPersistentStorage persistentStorage;

    public SparkassenTransactionsFetcher(
            SparkassenApiClient apiClient, SparkassenPersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        return TransactionMapper.tryParseXmlResponse(
                        apiClient.fetchTransactions(
                                persistentStorage.getConsentId(),
                                account.getApiIdentifier(),
                                getFetchStartDate()))
                .map(
                        fetchTransactionsResponse ->
                                fetchTransactionsResponse.getBkToCstmrAcctRpt().getRpt().stream()
                                        .flatMap(x -> x.getEntries().stream())
                                        .map(TransactionMapper::toTinkTransaction)
                                        .collect(Collectors.toList()))
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        SparkassenConstants.ErrorMessages
                                                .COULD_NOT_PARSE_TRANSACTIONS));
    }

    private LocalDate getFetchStartDate() {
        LocalDate startDate;
        if (persistentStorage.isFirstFetch()) {
            startDate = LocalDate.ofEpochDay(0);
            persistentStorage.markFirstFetchAsDone();
        } else {
            startDate = LocalDate.now().minusDays(90);
        }
        return startDate;
    }
}
