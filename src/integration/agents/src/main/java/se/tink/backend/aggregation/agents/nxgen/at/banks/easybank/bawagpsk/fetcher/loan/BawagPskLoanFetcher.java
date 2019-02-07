package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher.loan;

import java.util.Collection;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher.BawagPskAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.GetAccountInformationListResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.libraries.pair.Pair;

public final class BawagPskLoanFetcher implements AccountFetcher<LoanAccount> {

    private final BawagPskAccountFetcher accountFetcher;

    public BawagPskLoanFetcher(BawagPskApiClient bawagPskApiClient) {
        accountFetcher = new BawagPskAccountFetcher(bawagPskApiClient);
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        final Pair<GetAccountInformationListResponse, Map<String, String>> pair =
                accountFetcher.fetchAccountData();

        return pair.first.extractLoanAccounts(pair.second);
    }
}
