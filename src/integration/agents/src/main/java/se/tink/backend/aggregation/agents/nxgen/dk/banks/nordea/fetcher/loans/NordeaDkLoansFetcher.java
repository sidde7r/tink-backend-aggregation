package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
@RequiredArgsConstructor
public class NordeaDkLoansFetcher
        implements AccountFetcher<LoanAccount>, TransactionKeyPaginator<LoanAccount, String> {

    private final NordeaDkApiClient apiClient;

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return apiClient.getLoans().getLoans().stream()
                .map(LoanEntity::getLoanId)
                .map(apiClient::getLoanDetails)
                .map(LoanDetailsResponse::toTinkLoanAccount)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            LoanAccount account, String continuationKey) {
        TransactionsResponse transactionsResponse;
        try {
            transactionsResponse =
                    apiClient.getAccountTransactions(
                            account.getApiIdentifier(),
                            account.getFromTemporaryStorage(
                                    NordeaDkConstants.StorageKeys.PRODUCT_CODE),
                            continuationKey);

            log.info(
                    "[Nordea DK] Successfully fetched loan transactions. Loan type: {}, loan productCode: {}",
                    account.getDetails().getType(),
                    account.getFromTemporaryStorage(NordeaDkConstants.StorageKeys.PRODUCT_CODE));

            return new TransactionKeyPaginatorResponseImpl<>(
                    getTransactions(transactionsResponse),
                    transactionsResponse.getContinuationKey());
        } catch (HttpResponseException e) {
            log.info(
                    "[Nordea DK] Failed to fetch loan transactions. Loan type: {}, loan productCode: {} ",
                    account.getDetails().getType(),
                    account.getFromTemporaryStorage(NordeaDkConstants.StorageKeys.PRODUCT_CODE),
                    e);
            return TransactionKeyPaginatorResponseImpl.createEmpty();
        }
    }

    List<Transaction> getTransactions(TransactionsResponse transactionsResponse) {
        return transactionsResponse.getTransactions().stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
