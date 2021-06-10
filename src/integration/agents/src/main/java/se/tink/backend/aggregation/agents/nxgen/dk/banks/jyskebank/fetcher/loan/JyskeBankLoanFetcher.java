package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.loan;

import java.util.Collection;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.transactionalaccount.entities.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

@RequiredArgsConstructor
public class JyskeBankLoanFetcher
        implements AccountFetcher<LoanAccount>, TransactionPagePaginator<LoanAccount> {
    private final JyskeBankApiClient apiClient;

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        final AccountResponse accountResponse = apiClient.fetchAccounts();

        return accountResponse.stream()
                .filter(AccountsEntity::isLoanAccount)
                .map(AccountsEntity::toTinkLoanAccount)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(LoanAccount account, int page) {
        final TransactionResponse transactionResponse =
                apiClient.fetchTransactions(
                        account.getFromTemporaryStorage(Storage.PUBLIC_ID), page);

        return PaginatorResponseImpl.create(
                transactionResponse.toTinkTransactions(),
                transactionResponse.isHasMoreTransactions());
    }
}
