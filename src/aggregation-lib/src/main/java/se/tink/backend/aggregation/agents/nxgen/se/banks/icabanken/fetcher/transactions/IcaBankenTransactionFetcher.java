package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transactions;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transactions.entities.RootTransactionModel;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transactions.entities.TransactionsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class IcaBankenTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private final IcaBankenApiClient apiClient;
    private static final int LAST_WEEK_DAYS = -7;

    public IcaBankenTransactionFetcher(IcaBankenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<? extends Transaction> getTransactionsFor(TransactionalAccount account, Date fromDate,
            Date toDate) {
        RootTransactionModel transactionResponse = apiClient
                .fetchTransactions(account.getBankIdentifier(), dateFormatter(fromDate), dateFormatter(toDate));

        if (transactionResponse == null) {
            return Collections.emptyList();
        }

        List<TransactionsEntity> transactions = transactionResponse.getBody().getTransactions();

        if (includeReservations(toDate)) {
            RootTransactionModel reservedTransactionResponse = apiClient
                    .fetchReservedTransactions(account.getBankIdentifier());
            transactions.addAll(reservedTransactionResponse.getBody().getTransactions());

        }

        return transactions.stream().map(TransactionsEntity::toTinkTransaction).collect(Collectors.toList());
    }

    private boolean includeReservations(Date toDate) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, LAST_WEEK_DAYS);
        return toDate.after(c.getTime());
    }

    private String dateFormatter(Date date) {
        if (date == null) {
            return "";
        }

        return ThreadSafeDateFormat.FORMATTER_DAILY.format(date);
    }
}

