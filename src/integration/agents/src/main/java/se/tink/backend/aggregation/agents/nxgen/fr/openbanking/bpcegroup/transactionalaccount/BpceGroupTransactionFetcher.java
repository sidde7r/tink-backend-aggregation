package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceGroupApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class BpceGroupTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private final BpceGroupApiClient bpceGroupApiClient;

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        final String resourceId = account.getApiIdentifier();

        return bpceGroupApiClient.getTransactions(
                resourceId, convertDateToLocalDate(fromDate), convertDateToLocalDate(toDate));
    }

    private static LocalDate convertDateToLocalDate(Date source) {
        return source.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
