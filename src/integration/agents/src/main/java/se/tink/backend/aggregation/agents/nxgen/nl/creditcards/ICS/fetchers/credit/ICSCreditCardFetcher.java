package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit;

import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class ICSCreditCardFetcher implements TransactionPagePaginator<CreditCardAccount> {

    private final ICSApiClient client;

    public ICSCreditCardFetcher(ICSApiClient client) {
        this.client = client;
    }

    // At this time they do not support pagination
    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        return client.getTransactions(account.getFromTemporaryStorage(StorageKeys.ACCOUNT_ID));
    }
}
