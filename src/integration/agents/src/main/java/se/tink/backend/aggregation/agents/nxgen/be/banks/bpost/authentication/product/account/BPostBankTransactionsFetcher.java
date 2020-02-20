package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.product.account;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.BPostBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankAuthContext;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BPostBankTransactionsFetcher
        implements TransactionPagePaginator<TransactionalAccount> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    static final int TRANSACTIONS_PAGE_SIZE = 21;
    private BPostBankApiClient apiClient;
    private BPostBankAuthContext authContext;

    public BPostBankTransactionsFetcher(
            BPostBankApiClient apiClient, BPostBankAuthContext authContext) {
        this.apiClient = apiClient;
        this.authContext = authContext;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        try {
            List<BPostBankTransactionDTO> transactions =
                    apiClient.fetchAccountTransactions(
                            account, page, TRANSACTIONS_PAGE_SIZE, authContext);
            return new PaginatorResponse() {
                @Override
                public Collection<? extends Transaction> getTinkTransactions() {
                    return transactions.stream()
                            .map(t -> mapToTinkTransaction(t))
                            .collect(Collectors.toList());
                }

                @Override
                public Optional<Boolean> canFetchMore() {
                    return Optional.of(
                            !transactions.isEmpty()
                                    && transactions.size() == TRANSACTIONS_PAGE_SIZE);
                }
            };
        } catch (RequestException ex) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(ex.getMessage());
        }
    }

    private Transaction mapToTinkTransaction(BPostBankTransactionDTO transactionDTO) {
        Transaction.Builder tb = new Transaction.Builder();
        tb.setDate(
                LocalDateTime.parse(transactionDTO.bookingDateTime, DATE_TIME_FORMATTER)
                        .toLocalDate());
        tb.setDescription(transactionDTO.categoryId);
        tb.setAmount(
                ExactCurrencyAmount.of(
                        transactionDTO.transactionAmount, transactionDTO.transactionCurrency));
        return tb.build();
    }
}
