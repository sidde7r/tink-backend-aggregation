package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher.creditcard;

import java.util.Collection;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher.BawagPskAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.GetAccountInformationListResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.pair.Pair;

public final class BawagPskCreditCardFetcher implements AccountFetcher<CreditCardAccount> {

    private final BawagPskAccountFetcher accountFetcher;

    public BawagPskCreditCardFetcher(BawagPskApiClient bawagPskApiClient) {
        accountFetcher = new BawagPskAccountFetcher(bawagPskApiClient);
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        final Pair<GetAccountInformationListResponse, Map<String, String>> pair =
                accountFetcher.fetchAccountData();

        return pair.first.extractCreditCardAccounts(pair.second);
    }
}
