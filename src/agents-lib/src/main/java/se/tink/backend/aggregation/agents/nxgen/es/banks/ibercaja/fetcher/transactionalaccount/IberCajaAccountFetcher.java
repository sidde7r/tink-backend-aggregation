package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants.Storage.TICKET;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants.Storage.USERNAME;

public class IberCajaAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final IberCajaApiClient bankClient;
    private final SessionStorage storage;

    public IberCajaAccountFetcher(IberCajaApiClient bankClient,
            SessionStorage storage) {
        this.bankClient = bankClient;
        this.storage = storage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        FetchAccountResponse fetchAccount = bankClient.fetchAccountList(storage.get(TICKET), storage.get(USERNAME));
        return fetchAccount.getAccounts();
    }
}
