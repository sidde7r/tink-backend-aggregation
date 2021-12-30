package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.fetcher;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@RequiredArgsConstructor
@Slf4j
public class FinTecSystemsAccountReportFetcher
        implements AccountFetcher<TransactionalAccount>, TransactionFetcher<TransactionalAccount> {

    private final FinTecSystemsApiClient apiClient;
    private final FinTecSystemsStorage storage;
    private final FinTecSystemsReportMapper reportMapper;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        Optional<String> maybeTransactionId = storage.retrieveTransactionId();

        if (!maybeTransactionId.isPresent()) {
            log.warn(
                    "FTS Agent was executed in account-fetch scenario before successful payment! Will return no accounts.");
            return Collections.emptyList();
        }

        return reportMapper
                .transformReportToTinkAccount(apiClient.fetchReport(maybeTransactionId.get()))
                .map(Collections::singletonList)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "FTS Agent failed to parse payment report! This should not happen."));
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        return Collections.emptyList();
    }
}
