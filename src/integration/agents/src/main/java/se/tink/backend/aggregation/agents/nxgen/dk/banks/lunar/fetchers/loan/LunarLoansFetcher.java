package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.loan;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.client.FetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.loan.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.LunarIdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@Slf4j
@RequiredArgsConstructor
public class LunarLoansFetcher
        implements AccountFetcher<LoanAccount>, TransactionPaginator<LoanAccount> {

    private final FetcherApiClient apiClient;
    private final LunarIdentityDataFetcher identityDataFetcher;
    private List<LoanEntity> lunarLoans = Collections.emptyList();

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        lunarLoans = apiClient.fetchLoans().getLoans();
        return lunarLoans.stream()
                .filter(loanEntity -> BooleanUtils.isNotTrue(loanEntity.getDeleted()))
                .map(loanEntity -> loanEntity.toTinkLoan(identityDataFetcher.getAccountHolder()))
                .collect(Collectors.toList());
    }

    @Override
    public void resetState() {
        // there is nothing to reset, because there is no pagination in loans transactions
    }

    @Override
    public PaginatorResponse fetchTransactionsFor(LoanAccount account) {
        List<Transaction> transactions =
                lunarLoans.stream()
                        .filter(loanEntity -> accountIdEquals(account, loanEntity))
                        .flatMap(this::getAccountTransactions)
                        .collect(Collectors.toList());
        return PaginatorResponseImpl.create(transactions, false);
    }

    private boolean accountIdEquals(LoanAccount account, LoanEntity loanEntity) {
        return account.getAccountNumber().equals(loanEntity.getAccountId());
    }

    private Stream<Transaction> getAccountTransactions(LoanEntity loanEntity) {
        return loanEntity.getTransactions().stream().map(TransactionEntity::toTinkTransaction);
    }
}
