package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers;

import com.google.common.collect.Lists;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.FutureTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.FutureTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.libraries.date.DateUtils;

public class DanskeBankMultiTransactionsFetcher<A extends Account>
        implements TransactionDatePaginator<A>, UpcomingTransactionFetcher<A> {
    private static final ZoneId TIMEZONE = TimeZone.getTimeZone("Europe/Stockholm").toZoneId();

    private final DanskeBankApiClient apiClient;
    private final String languageCode;
    private boolean hasSuccessfullyFetched;

    public DanskeBankMultiTransactionsFetcher(DanskeBankApiClient apiClient, String languageCode) {
        this.apiClient = apiClient;
        this.languageCode = languageCode;
    }

    @Override
    public PaginatorResponse getTransactionsFor(A account, Date fromDate, Date toDate) {
        String from =
                fromDate.toInstant()
                        .atZone(TIMEZONE)
                        .toLocalDate()
                        .format(DateTimeFormatter.BASIC_ISO_DATE);
        String to =
                toDate.toInstant()
                        .atZone(TIMEZONE)
                        .toLocalDate()
                        .format(DateTimeFormatter.BASIC_ISO_DATE);

        ListTransactionsRequest listTransactionsRequest =
                ListTransactionsRequest.create(
                        this.languageCode, account.getBankIdentifier(), from, to);
        ListTransactionsResponse listTransactionsResponse;

        // Danske Bank has a limit of how far back in time we can fetch transactions.
        // If we fetch further then they allow they will respond with a Http status in the range
        // >=400.
        try {
            listTransactionsResponse = this.apiClient.listTransactions(listTransactionsRequest);
        } catch (HttpResponseException e) {
            // If we are able to deserialize the response we can be certain that we have made a
            // successful
            // request but Danske Bank limits how far back we can fetch.
            e.getResponse().getBody(ListTransactionsResponse.class);
            return PaginatorResponseImpl.createEmpty();
        }

        List<Transaction> transactions = Lists.newArrayList();
        transactions.addAll(
                listTransactionsResponse.getTransactions().stream()
                        .map(TransactionEntity::toTinkTransaction)
                        .collect(Collectors.toList()));

        // Danske Bank has some kind of date paginator combined with page paginator.
        // We should however never trigger this since we allow 9999 transactions.
        while (!listTransactionsResponse.isEndOfList()) {
            listTransactionsRequest.setRepositionKey(listTransactionsResponse.getRepositionKey());
            listTransactionsResponse = this.apiClient.listTransactions(listTransactionsRequest);

            transactions.addAll(
                    listTransactionsResponse.getTransactions().stream()
                            .map(TransactionEntity::toTinkTransaction)
                            .collect(Collectors.toList()));
        }

        // TODO: Removing `toDate.after(DateUtils.addYears(new Date(), -2))` will cause paginator to
        // sometimes get
        // TODO: stuck looping the last transactions forever. -2 is based on transaction range in
        // app. Find better fix.
        return PaginatorResponseImpl.create(
                transactions, toDate.after(DateUtils.addYears(new Date(), -2)));
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(A account) {
        FutureTransactionsResponse futureTransactionsResponse =
                this.apiClient.listUpcomingTransactions(
                        FutureTransactionsRequest.create(
                                this.languageCode, account.getBankIdentifier()));

        return futureTransactionsResponse.getTransactions().stream()
                .map(TransactionEntity::toTinkUpcomingTransaction)
                .collect(Collectors.toList());
    }
}
