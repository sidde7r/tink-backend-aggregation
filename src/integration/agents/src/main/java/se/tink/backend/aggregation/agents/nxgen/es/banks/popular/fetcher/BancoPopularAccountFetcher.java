package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher;

import java.util.ArrayList;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities.BancoPopularContract;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc.FetchAccountsRequest;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class BancoPopularAccountFetcher extends BancoPopularContractFetcher implements
        AccountFetcher<TransactionalAccount> {

    public BancoPopularAccountFetcher(BancoPopularApiClient bankClient,
            BancoPopularPersistentStorage persistentStorage) {
        super(bankClient, persistentStorage);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        Collection<TransactionalAccount> allAccounts = new ArrayList<>();

        Collection<BancoPopularContract> contracts = fetchContracts();

        for (BancoPopularContract contract : contracts) {

            if (selectCurrentContract(contract)) {

                FetchAccountsRequest fetchAccountsRequest = FetchAccountsRequest.build(
                        BancoPopularConstants.Fetcher.CHECKING_ACCOUNT_IDENTIFIER);

                allAccounts.addAll(bankClient.fetchAccounts(fetchAccountsRequest)
                        .getTinkAccounts());
            }
        }

        return allAccounts;
    }
}
