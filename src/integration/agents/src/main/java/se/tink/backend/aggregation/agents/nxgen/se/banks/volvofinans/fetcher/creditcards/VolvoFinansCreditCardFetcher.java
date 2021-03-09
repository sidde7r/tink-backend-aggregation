package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards;

import static org.apache.commons.lang3.ObjectUtils.max;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.date.DateUtils;

public class VolvoFinansCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionDatePaginator<CreditCardAccount> {

    private final VolvoFinansApiClient apiClient;

    public VolvoFinansCreditCardFetcher(VolvoFinansApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        try {

            return apiClient.creditCardAccounts().stream()
                    .map(a -> a.toTinkAccount(apiClient.creditCardData()))
                    .collect(Collectors.toList());
        } catch (HttpResponseException hre) {
            // When user doesn't have any credit cards we get NOT FOUND as response
            if (hre.getResponse().getStatus() == HttpStatus.SC_NOT_FOUND) {
                return Collections.emptyList();
            }

            throw hre;
        }
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {

        LocalDate localStartDate = DateUtils.toJavaTimeLocalDate(fromDate);
        LocalDate localToDate = DateUtils.toJavaTimeLocalDate(toDate);

        List<CreditCardTransaction> transactions = new ArrayList<>();

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

    private List<CreditCardTransaction> getTransactionsBatch(
            CreditCardAccount account, LocalDate localFromDate, LocalDate localToDate) {
        String accountId =
                account.getFromTemporaryStorage(VolvoFinansConstants.UrlParameters.ACCOUNT_ID);
        int limit = VolvoFinansConstants.Pagination.LIMIT;
        int offset = 0;

        List<CreditCardTransaction> transactions = new ArrayList<>();

        boolean pagesLeft = true;
        while (pagesLeft) {
            List<CreditCardTransaction> collected =
                    apiClient
                            .creditCardAccountTransactions(
                                    accountId, localFromDate, localToDate, limit, offset)
                            .stream()
                            .map(CreditCardTransactionEntity::toTinkTransaction)
                            .collect(Collectors.toList());
            transactions.addAll(collected);

            pagesLeft = collected.size() >= limit;
            offset += limit;
        }
        return transactions;
    }
}
