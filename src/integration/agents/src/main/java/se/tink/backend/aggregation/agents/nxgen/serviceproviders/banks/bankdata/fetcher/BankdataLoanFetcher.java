package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.GetTransactionsRequest;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

@RequiredArgsConstructor
public final class BankdataLoanFetcher
        implements AccountFetcher<LoanAccount>, TransactionPagePaginator<LoanAccount> {

    private final BankdataApiClient bankdataApiClient;

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return BankdataLoanMapper.getLoanAccounts(bankdataApiClient.getAccounts());
    }

    @Override
    public PaginatorResponse getTransactionsFor(LoanAccount account, int page) {
        GetTransactionsRequest getTransactionsRequest =
                new GetTransactionsRequest()
                        .addAccount(
                                account.getFromTemporaryStorage(
                                        BankdataAccountEntity.REGISTRATION_NUMBER_TEMP_STORAGE_KEY),
                                account.getFromTemporaryStorage(
                                        BankdataAccountEntity.ACCOUNT_NUMBER_TEMP_STORAGE_KEY))
                        .setPage(page);

        return bankdataApiClient.getTransactions(getTransactionsRequest);
    }
}
