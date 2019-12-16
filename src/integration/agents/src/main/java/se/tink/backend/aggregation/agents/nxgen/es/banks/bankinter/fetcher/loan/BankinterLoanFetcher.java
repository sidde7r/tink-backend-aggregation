package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.loan;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.loan.rpc.LoanResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class BankinterLoanFetcher
        implements AccountFetcher<LoanAccount>, TransactionKeyPaginator<LoanAccount, String> {
    private final BankinterApiClient apiClient;

    public BankinterLoanFetcher(BankinterApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return this.apiClient.fetchGlobalPosition().getLoanLinks().stream()
                .map(apiClient::fetchLoan)
                .map(LoanResponse::toLoanAccount)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            LoanAccount account, String key) {
        return new TransactionKeyPaginatorResponseImpl<>();
    }
}
