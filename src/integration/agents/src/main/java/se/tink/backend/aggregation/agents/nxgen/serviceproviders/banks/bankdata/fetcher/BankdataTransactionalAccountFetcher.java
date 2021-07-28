package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher;

import java.util.List;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class BankdataTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final BankdataApiClient bankClient;

    @Override
    public List<TransactionalAccount> fetchAccounts() {
        return BankdataTransactionalAccountsMapper.getTransactionalAccounts(
                bankClient.getAccounts());
    }
}
