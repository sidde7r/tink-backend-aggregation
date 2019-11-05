package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.investments;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioConstants;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.investments.entities.InvestmentAccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class MontepioInvestmentAccountsFetcher
        implements AccountFetcher<InvestmentAccount>, TransactionPagePaginator<InvestmentAccount> {

    private final MontepioApiClient apiClient;

    public MontepioInvestmentAccountsFetcher(MontepioApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient);
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        return apiClient.fetchSavingsAccounts().getResult().getAccounts().stream()
                .map(InvestmentAccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(InvestmentAccount account, int page) {
        LocalDate to = LocalDate.now();
        LocalDate from =
                LocalDate.now().minusMonths(MontepioConstants.MAX_TRANSACTION_HISTORY_MONTHS);

        FetchTransactionsResponse response =
                apiClient.fetchSavingsAccountTransactions(account, page, from, to);

        response.getError()
                .ifPresent(
                        errorEntity -> {
                            throw new IllegalStateException(
                                    String.format(
                                            MontepioConstants.TRANSACTIONS_FETCH_ERROR_FORMAT,
                                            errorEntity.getCode(),
                                            errorEntity.getMessage()));
                        });

        List<Transaction> transactions =
                response.getResultEntity().getTransactions().orElseGet(Collections::emptyList)
                        .stream()
                        .map(TransactionEntity::toTinkTransaction)
                        .collect(Collectors.toList());
        return PaginatorResponseImpl.create(
                transactions, response.getResultEntity().hasMorePages());
    }
}
