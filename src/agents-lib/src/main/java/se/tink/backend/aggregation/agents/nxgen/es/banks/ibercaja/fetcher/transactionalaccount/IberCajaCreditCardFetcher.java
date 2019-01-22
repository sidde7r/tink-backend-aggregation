package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;

public class IberCajaCreditCardFetcher implements AccountFetcher<CreditCardAccount> {

    private final IberCajaApiClient bankClient;

    public IberCajaCreditCardFetcher(IberCajaApiClient bankClient) {
        this.bankClient = bankClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        FetchAccountResponse fetchCreditCard = bankClient.fetchCreditCardsList();
        return fetchCreditCard.getCreditCardAccounts();
    }
}
