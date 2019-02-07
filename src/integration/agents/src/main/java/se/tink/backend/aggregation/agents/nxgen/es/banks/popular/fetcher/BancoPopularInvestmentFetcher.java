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
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class BancoPopularInvestmentFetcher extends BancoPopularContractFetcher implements AccountFetcher<InvestmentAccount> {
    private static final AggregationLogger log = new AggregationLogger(BancoPopularInvestmentFetcher.class);

    public BancoPopularInvestmentFetcher(BancoPopularApiClient bankClient,
            BancoPopularPersistentStorage persistentStorage) {
        super(bankClient, persistentStorage);
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        try {
            Collection<BancoPopularContract> contracts = fetchContracts();

            for (BancoPopularContract contract : contracts) {
                if (selectCurrentContract(contract)) {
                    fetchFundAccounts();
                    fetchSecuritiesAccounts();
                    fetchCreditAccounts();
                }
            }

        } catch (Exception e) {
            log.infoExtraLong("Could not fetch investment accounts " + e.toString(),
                    BancoPopularConstants.Fetcher.INVESTMENT_LOGGING);
        }

        return Collections.emptyList();
    }

    private void fetchFundAccounts() {
        FetchAccountsRequest fetchInvestmentAccountsRequest = FetchAccountsRequest.build(
                BancoPopularConstants.Fetcher.FUND_ACCOUNT_IDENTIFIER);

        String fetchInvestmentAccountsResponse = bankClient.fetchFundAccounts(fetchInvestmentAccountsRequest);
        // this is just to avoid unnecessary logging when no data is present
        if (fetchInvestmentAccountsResponse.contains("\"faultIndicator\":true")) {
            log.debug("Unable to fetch fund accounts " + fetchInvestmentAccountsResponse);
        } else {
            log.infoExtraLong(fetchInvestmentAccountsResponse, BancoPopularConstants.Fetcher.INVESTMENT_LOGGING);
        }
    }

    private void fetchSecuritiesAccounts() {
        FetchAccountsRequest fetchInvestmentAccountsRequest = FetchAccountsRequest.build(
                BancoPopularConstants.Fetcher.INSURANCE_ACCOUNT_IDENTIFIER);

        String fetchInvestmentAccountsResponse = bankClient.fetchSecuritiesAccounts(fetchInvestmentAccountsRequest);
        // this is just to avoid unnecessary logging when no data is present
        if (fetchInvestmentAccountsResponse.contains("\"faultIndicator\":true")) {
            log.debug("Unable to fetch securities accounts " + fetchInvestmentAccountsResponse);
        } else {
            log.infoExtraLong(fetchInvestmentAccountsResponse, BancoPopularConstants.Fetcher.INVESTMENT_LOGGING);
        }
    }

    private void fetchCreditAccounts() {
        FetchAccountsRequest request =  FetchAccountsRequest.build(
                BancoPopularConstants.Fetcher.CREDIT_CARD_ACCOUNT_IDENTIFIER);

        String response = bankClient.fetchCreditAccounts(request);
        // this is just to avoid unnecessary logging when no data is present
        if (response.contains("\"faultIndicator\":true")) {
            log.debug("Unable to fetch credit accounts " + response);
        } else {
            log.infoExtraLong(response, BancoPopularConstants.Fetcher.INVESTMENT_LOGGING);
        }
    }
}
