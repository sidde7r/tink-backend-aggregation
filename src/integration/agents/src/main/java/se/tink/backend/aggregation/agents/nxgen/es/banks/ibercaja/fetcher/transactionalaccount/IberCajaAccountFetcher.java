package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class IberCajaAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final IberCajaApiClient bankClient;
    private final IberCajaSessionStorage sessionStorage;

    public IberCajaAccountFetcher(
            IberCajaApiClient bankClient, IberCajaSessionStorage sessionStorage) {
        this.bankClient = bankClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return bankClient.fetchAccountList().getAccounts(sessionStorage);
    }
}
