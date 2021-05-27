package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.fetcher;

import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.transaction.FinTsTransactionMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@Slf4j
public class BankverlagTransactionsFetcher implements TransactionFetcher<TransactionalAccount> {
    private final BankverlagApiClient apiClient;
    private final BankverlagStorage storage;
    // Reusing FinTsTransactionMapper as Bankverlag also provides transactions in Swift format
    // later move this mapper to common util
    FinTsTransactionMapper mapper = new FinTsTransactionMapper();

    public BankverlagTransactionsFetcher(BankverlagApiClient apiClient, BankverlagStorage storage) {
        this.apiClient = apiClient;
        this.storage = storage;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {

        List<AggregationTransaction> aggregationTransactions = null;
        try {
            aggregationTransactions =
                    mapper.parseSwift(
                            apiClient.fetchTransactions(
                                    storage.getConsentId(),
                                    account.getApiIdentifier(),
                                    getFetchStartDate()));

        } catch (Exception e) {
            log.error("Unable to parse transactions", e);
        }

        return aggregationTransactions;
    }

    private LocalDate getFetchStartDate() {
        LocalDate startDate;
        if (storage.isFirstFetch()) {
            startDate = LocalDate.ofEpochDay(0);
            storage.markFirstFetchAsDone();
        } else {
            startDate = LocalDate.now().minusDays(89);
        }
        return startDate;
    }
}
