package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
        String productCode =
                account.getFromTemporaryStorage(NordeaDkConstants.StorageKeys.PRODUCT_CODE);
        try {
            transactionsResponse =
                    apiClient.getAccountTransactions(
                            account.getApiIdentifier(), productCode, continuationKey);

            log.info(
                    "[Nordea DK] Successfully fetched loan transactions. Loan type: {}, loan productCode: {}",
                    account.getDetails().getType(),
                    productCode);

            return new TransactionKeyPaginatorResponseImpl<>(
                    getTransactions(transactionsResponse),
                    transactionsResponse.getContinuationKey());
        } catch (HttpResponseException e) {
            log.info(
                    "[Nordea DK] Failed to fetch loan transactions. Loan type: {}, loan productCode: {} ",
                    account.getDetails().getType(),
                    productCode,
                    e);
            return TransactionKeyPaginatorResponseImpl.createEmpty();
        }
    }

    private List<Transaction> getTransactions(TransactionsResponse transactionsResponse) {
        return Optional.ofNullable(transactionsResponse.getTransactions())
                .orElse(Collections.emptyList()).stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
