package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaConstants.STORAGE;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc.MortgageInstallmentsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

public class CaixaLoanAccountFetcher
        implements AccountFetcher<LoanAccount>, TransactionKeyPaginator<LoanAccount, String> {

    private static final Logger log = LoggerFactory.getLogger(CaixaLoanAccountFetcher.class);
    private final CaixaApiClient apiClient;

    public CaixaLoanAccountFetcher(CaixaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return CollectionUtils.union(fetchConsumerLoans(), fetchMortgages());
    }

    private List<LoanAccount> fetchMortgages() {
        return apiClient.fetchMortgageLoanAccounts().getAccounts().stream()
                .map(
                        loanAcc ->
                                loanAcc.mortgageToTinkAccount(
                                        apiClient.fetchMortgageDetails(
                                                loanAcc.getFullAccountKey())))
                .collect(Collectors.toList());
    }

    private List<LoanAccount> fetchConsumerLoans() {
        if (CollectionUtils.isNotEmpty(apiClient.fetchConsumerLoanAccounts().getAccounts())) {
            log.warn("Mapping of consumer loans not implemented - skipping.");
        }
        return Collections.emptyList();
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            LoanAccount account, String key) {

        if (account.getDetails().getType().equals(Type.MORTGAGE)) {
            return getMortgageInstallments(account, key);
        } else {
            throw new NotImplementedException(
                    "Handling accounts different than mortgages not implemented.");
        }
    }

    private TransactionKeyPaginatorResponse<String> getMortgageInstallments(
            LoanAccount account, String key) {
        MortgageInstallmentsResponse response =
                apiClient.fetchMortgageInstallments(account.getApiIdentifier(), key);

        List<Transaction> installments =
                response.getInstallments().stream()
                        .map(
                                installment ->
                                        installment.toTinkTransaction(
                                                account.getFromTemporaryStorage(
                                                        STORAGE.ACCOUNT_CURRENCY)))
                        .collect(Collectors.toList());

        String nextPageKey = response.getIsLastPage() ? null : response.getNextPageKey();
        return new TransactionKeyPaginatorResponseImpl<>(installments, nextPageKey);
    }
}
