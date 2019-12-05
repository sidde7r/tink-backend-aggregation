package se.tink.sa.framework.facade;

import se.tink.sa.services.fetch.account.FetchAccountsRequest;
import se.tink.sa.services.fetch.account.FetchAccountsResponse;
import se.tink.sa.services.fetch.trans.FetchTransactionsRequest;
import se.tink.sa.services.fetch.trans.FetchTransactionsResponse;

public interface AccountInformationFacade {

    FetchAccountsResponse fetchCheckingAccounts(FetchAccountsRequest request);

    FetchTransactionsResponse fetchCheckingAccountsTransactions(FetchTransactionsRequest request);
}
