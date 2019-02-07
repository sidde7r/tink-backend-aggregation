package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher.investment;

import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher.BawagPskAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.GetAccountInformationListResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.pair.Pair;

import java.util.Collection;
import java.util.Map;

public final class BawagPskInvestmentFetcher implements AccountFetcher<InvestmentAccount> {

    private final BawagPskAccountFetcher accountFetcher;

    public BawagPskInvestmentFetcher(BawagPskApiClient bawagPskApiClient) {
        accountFetcher = new BawagPskAccountFetcher(bawagPskApiClient);
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        final Pair<GetAccountInformationListResponse, Map<String, String>> pair =
                accountFetcher.fetchAccountData();

        return pair.first.extractInvestmentAccounts(pair.second);
    }
}
