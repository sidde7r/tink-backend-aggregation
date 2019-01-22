package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount;

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants.Storage.TICKET;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants.Storage.USERNAME;

public class IberCajaInvestmentAccountFetcher implements AccountFetcher<InvestmentAccount> {

    private static final Logger logger = LoggerFactory.getLogger(IberCajaInvestmentAccountFetcher.class);
    private final IberCajaApiClient bankClient;
    private final SessionStorage sessionStorage;

    public IberCajaInvestmentAccountFetcher(IberCajaApiClient bankClient,
            SessionStorage sessionStorage) {
        this.bankClient = bankClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        FetchAccountResponse accounts = bankClient
                .fetchInvestmentAccounList(sessionStorage.get(TICKET), sessionStorage.get(USERNAME));
        Collection<InvestmentAccount> investmentAccounts = accounts.getInvestmentAccounts();

        for (InvestmentAccount i : investmentAccounts) {
            String investmentResponse = bankClient
                    .fetchInvestmentTransactionDetails(i.getBankIdentifier(), sessionStorage.get(TICKET),
                            sessionStorage.get(USERNAME));
            logger.info(investmentResponse);
        }

        return investmentAccounts;
    }
}
