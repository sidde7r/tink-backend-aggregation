package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.fetcher;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.client.SpardaFetcherApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities.FetcherLinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.mappers.TransactionMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@RequiredArgsConstructor
@Slf4j
public class SpardaTransactionFetcher implements TransactionFetcher<TransactionalAccount> {

    private final SpardaFetcherApiClient apiClient;
    private final TransactionMapper spardaTransactionMapper;
    private final TransactionPaginationHelper transactionPaginationHelper;
    private final LocalDateTimeSource localDateTimeSource;

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        FetchTransactionsResponse fetchTransactionsResponse;
        fetchTransactionsResponse =
                apiClient.fetchTransactions(account.getApiIdentifier(), getStartDate(account));

        List<AggregationTransaction> result =
                spardaTransactionMapper.toTinkTransactions(
                        fetchTransactionsResponse.getTransactions());

        Optional<String> nextUrl = extractNextUrl(fetchTransactionsResponse);
        while (nextUrl.isPresent()) {
            fetchTransactionsResponse = apiClient.fetchTransactions(nextUrl.get());

            result.addAll(
                    spardaTransactionMapper.toTinkTransactions(
                            fetchTransactionsResponse.getTransactions()));
            nextUrl = extractNextUrl(fetchTransactionsResponse);
        }

        return result;
    }

    private LocalDate getStartDate(TransactionalAccount account) {
        return transactionPaginationHelper
                .getTransactionDateLimit(account)
                .map(
                        dateLimit ->
                                dateLimit.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                // Sparda only allows to fetch 90 days when using recurring consent
                .orElse(localDateTimeSource.now().toLocalDate().minusDays(90));
    }

    private Optional<String> extractNextUrl(FetchTransactionsResponse transactionsResponse) {
        return Optional.ofNullable(transactionsResponse.getLinks())
                .map(FetcherLinksEntity::getNext)
                .map(Href::getHref);
    }
}
