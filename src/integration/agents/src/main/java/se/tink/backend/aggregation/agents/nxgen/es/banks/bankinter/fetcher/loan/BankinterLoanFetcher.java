package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.loan;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.loan.entities.PaginationKey;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.loan.rpc.LoanResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class BankinterLoanFetcher
        implements AccountFetcher<LoanAccount>,
                TransactionKeyPaginator<LoanAccount, PaginationKey> {
    private final BankinterApiClient apiClient;

    public BankinterLoanFetcher(BankinterApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return this.apiClient.fetchGlobalPosition().getLoanLinks().stream()
                .map(apiClient::fetchLoan)
                .filter(LoanResponse::isSingleCurrency)
                .map(LoanResponse::toLoanAccount)
                .collect(Collectors.toList());
    }

    private LoanResponse fetchLoanPage(@Nonnull PaginationKey key) {
        return apiClient.fetchLoanPage(key.getSource(), key.getViewState(), key.getOffset());
    }

    @Override
    public TransactionKeyPaginatorResponse<PaginationKey> getTransactionsFor(
            LoanAccount account, PaginationKey key) {
        if (Objects.isNull(key)) {
            key =
                    account.getFromTemporaryStorage(
                                    StorageKeys.FIRST_PAGINATION_KEY, PaginationKey.class)
                            .get();
        }

        final LoanResponse page = fetchLoanPage(key);
        final List<? extends Transaction> transactions = page.toTinkTransactions(key.getSkip());
        final PaginationKey nextKey = transactions.isEmpty() ? null : page.getNextPaginationKey();
        return new TransactionKeyPaginatorResponseImpl<>(transactions, nextKey);
    }
}
