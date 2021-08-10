package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.loan;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.loan.entities.HomesEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.loan.entities.LoansEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.loan.rpc.MortgageDTO;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.loan.rpc.MortgageResponse;
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
        final MortgageResponse mortgageResponse = apiClient.fetchMortgages();

        final Collection<LoanAccount> loanAccounts =
                accountResponse.stream()
                        .filter(AccountsEntity::isLoanAccount)
                        .map(AccountsEntity::toTinkLoanAccount)
                        .collect(Collectors.toList());

        for (HomesEntity homesEntity : mortgageResponse.getHomes()) {
            final Collection<LoanAccount> mortgages =
                    homesEntity.getLoans().stream()
                            .map(LoansEntity::getDocumentId)
                            .map(id -> apiClient.fetchMortgageDetails(homesEntity, id))
                            .map(MortgageDTO::toTinkLoanAccount)
                            .collect(Collectors.toList());

            loanAccounts.addAll(mortgages);
        }

        return loanAccounts;
    }

    @Override
    public PaginatorResponse getTransactionsFor(LoanAccount account, int page) {
        final String accountId = account.getFromTemporaryStorage(Storage.PUBLIC_ID);
        // Mortgages does not have accountId to fetch transactions for
        if (Strings.isNullOrEmpty(accountId)) {
            return PaginatorResponseImpl.createEmpty();
        }
        final TransactionResponse transactionResponse =
                apiClient.fetchTransactions(accountId, page);

        return PaginatorResponseImpl.create(
                transactionResponse.toTinkTransactions(),
                transactionResponse.isHasMoreTransactions());
    }
}
