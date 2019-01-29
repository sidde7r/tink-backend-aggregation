package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities.BancoPopularContract;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc.FetchAccountsRequest;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class BancoPopularLoanFetcher extends BancoPopularContractFetcher implements AccountFetcher<LoanAccount> {
    private static final AggregationLogger log = new AggregationLogger(BancoPopularLoanFetcher.class);

    public BancoPopularLoanFetcher(BancoPopularApiClient bankClient, BancoPopularPersistentStorage persistentStorage) {
        super(bankClient, persistentStorage);
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        try {
            Collection<BancoPopularContract> contracts = fetchContracts();

            for (BancoPopularContract contract : contracts) {

                if (selectCurrentContract(contract)) {
                    FetchAccountsRequest fetchLoanAccountsRequest = FetchAccountsRequest.build(
                            BancoPopularConstants.Fetcher.LOAN_ACCOUNT_IDENTIFIER);

                    String fetchLoanAccountsResponse = bankClient.fetchLoanAccounts(fetchLoanAccountsRequest);
                    if (fetchLoanAccountsResponse.contains("\"faultIndicator\":true")) {
                        log.debug("Unable to fetch loan accounts " + fetchLoanAccountsResponse);
                    } else {
                        log.infoExtraLong(fetchLoanAccountsResponse, BancoPopularConstants.Fetcher.LOAN_LOGGING);
                    }
                }
            }

        } catch(Exception e) {
            log.infoExtraLong("Could not fetch loan accounts " + e.toString(), BancoPopularConstants.Fetcher.LOAN_LOGGING);
        }

        return Collections.emptyList();
    }
}
