package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants.Storage.TICKET;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants.Storage.USERNAME;

public class IberCajaCreditCardFetcher implements AccountFetcher<CreditCardAccount> {

    private final IberCajaApiClient bankClient;
    private final SessionStorage storage;

    public IberCajaCreditCardFetcher(IberCajaApiClient bankClient,
            SessionStorage storage) {
        this.bankClient = bankClient;
        this.storage = storage;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        FetchAccountResponse fetchCreditCard = bankClient.fetchCreditCardsList(storage.get(TICKET), storage.get(USERNAME));
        return fetchCreditCard.getCreditCardAccounts();
    }
}
