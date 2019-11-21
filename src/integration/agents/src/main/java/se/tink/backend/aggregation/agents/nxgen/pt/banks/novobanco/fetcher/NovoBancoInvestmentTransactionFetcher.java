package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher;

import java.util.Collections;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class NovoBancoInvestmentTransactionFetcher
        implements TransactionPagePaginator<InvestmentAccount> {
    @Override
    public PaginatorResponse getTransactionsFor(InvestmentAccount account, int page) {
        return PaginatorResponseImpl.create(
                Collections.emptyList(),
                false); // mobile app doers not support getting investment transactions
    }
}
