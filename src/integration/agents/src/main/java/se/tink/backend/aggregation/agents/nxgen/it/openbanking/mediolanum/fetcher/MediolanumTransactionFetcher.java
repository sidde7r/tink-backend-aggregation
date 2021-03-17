package se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.MediolanumApiClient;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@RequiredArgsConstructor
public class MediolanumTransactionFetcher implements TransactionFetcher<TransactionalAccount> {

    private final MediolanumApiClient apiClient;
    private final TransactionMapper transactionMapper;

    private final LocalDateTimeSource localDateTimeSource;
    private final boolean userPresent;

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        LocalDate today = localDateTimeSource.now().toLocalDate();
        LocalDate from = userPresent ? today.minusYears(5) : today.minusDays(90);
        return apiClient.fetchTransactions(account.getApiIdentifier(), from, today)
                .getTransactions().stream()
                .map(transactionMapper::toTinkTransaction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
