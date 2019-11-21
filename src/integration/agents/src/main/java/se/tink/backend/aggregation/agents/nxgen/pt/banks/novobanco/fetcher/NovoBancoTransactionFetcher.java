package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.detail.TransactionMapper;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.MovementsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.pair.Pair;

public class NovoBancoTransactionFetcher implements TransactionPagePaginator<TransactionalAccount> {
    private final NovoBancoApiClient apiClient;

    public NovoBancoTransactionFetcher(NovoBancoApiClient apiClient) {
        this.apiClient = requireNonNull(apiClient);
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        Pair<List<MovementsEntity>, String> movementsDetails =
                apiClient.getTransactions(account.getAccountNumber());
        String currency = movementsDetails.second;

        List<Transaction> transactions =
                Optional.ofNullable(movementsDetails.first)
                        .map(Collection::stream)
                        .orElse(Stream.empty())
                        .map(movement -> TransactionMapper.mapToTinkTransaction(movement, currency))
                        .collect(Collectors.toList());

        return PaginatorResponseImpl.create(transactions, false);
    }
}
