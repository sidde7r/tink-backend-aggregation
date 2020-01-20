package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class IberCajaInvestmentAccountFetcher implements AccountFetcher<InvestmentAccount> {

    private final IberCajaApiClient bankClient;

    public IberCajaInvestmentAccountFetcher(IberCajaApiClient bankClient) {
        this.bankClient = bankClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        FetchAccountResponse accounts = bankClient.fetchInvestmentAccounList();
        Collection<InvestmentAccount> investmentAccounts = accounts.getInvestmentAccounts();

        for (InvestmentAccount i : investmentAccounts) {
            // make the call so that the response will be logged to s3
            bankClient.fetchInvestmentTransactionDetails(i.getApiIdentifier());
        }

        return investmentAccounts;
    }
}
