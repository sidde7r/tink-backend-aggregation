package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactionalaccounts;

import static org.apache.commons.lang3.ObjectUtils.max;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactionalaccounts.rpc.SavingsAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.date.DateUtils;

public class VolvoFinansTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final VolvoFinansApiClient apiClient;

    public VolvoFinansTransactionalAccountFetcher(VolvoFinansApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        try {
            SavingsAccountsResponse savingsAccountsResponse = apiClient.savingsAccounts();
            return savingsAccountsResponse.getTinkAccounts();
        } catch (HttpResponseException hre) {
            // when user doesn't have savings we can get NOT FOUND (most often if the user doesn't
            // have credit card)
            if (hre.getResponse().getStatus() == HttpStatus.SC_NOT_FOUND) {
                return Collections.emptyList();
            }

            // this error is unknown, re-throw
            throw hre;
        }
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        LocalDate localStartDate = DateUtils.toJavaTimeLocalDate(fromDate);
        LocalDate localToDate = DateUtils.toJavaTimeLocalDate(toDate);

        List<Transaction> transactions = new ArrayList<>();

        /* outer loop sets time period to query for transactions */
        while (!localToDate.isBefore(localStartDate)) {
            /* set 'localFromDate' to first of month (or to 'localStartDate' if first of month is outside requested time period) */
            LocalDate localFromDate =
                    max(localToDate.minusDays(localToDate.getDayOfMonth() - 1L), localStartDate);
            transactions.addAll(getTransactionsBatch(account, localFromDate, localToDate));
            localToDate = localFromDate.minusDays(1);
        }

        return PaginatorResponseImpl.create(transactions);
    }

    private List<Transaction> getTransactionsBatch(
            Account account, LocalDate localFromDate, LocalDate localToDate) {
        String accountId = account.getApiIdentifier();
        int limit = VolvoFinansConstants.Pagination.LIMIT;
        int offset = 0;

        List<Transaction> transactions = new ArrayList<>();

        boolean pagesLeft = true;
        while (pagesLeft) {
            List<Transaction> collected =
                    apiClient
                            .savingsAccountTransactions(
                                    accountId, localFromDate, localToDate, limit, offset)
                            .getTinkTransactions();

            transactions.addAll(collected);

            pagesLeft = collected.size() >= limit;
            offset += limit;
        }
        return transactions;
    }
}
