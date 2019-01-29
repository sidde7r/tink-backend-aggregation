package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional;

import java.util.Collection;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.SantanderApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.SantanderConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class SantanderTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {
  private final SantanderApiClient santanderApiClient;

  public SantanderTransactionFetcher(SantanderApiClient santanderApiClient) {
    this.santanderApiClient = santanderApiClient;
  }

  @Override
  public PaginatorResponse getTransactionsFor(
      TransactionalAccount account, Date fromDate, Date toDate) {
    Collection<? extends Transaction> transactions =
        santanderApiClient
            .fetchTransactions(
                fromDate,
                toDate,
                account.getFromTemporaryStorage(SantanderConstants.STORAGE.LOCAL_CONTRACT_DETAIL),
                account.getFromTemporaryStorage((SantanderConstants.STORAGE.LOCAL_CONTRACT_TYPE)),
                account.getFromTemporaryStorage(SantanderConstants.STORAGE.COMPANY_ID))
            .toTinkTransactions();
    return PaginatorResponseImpl.create(transactions);
  }
}
